import re
import spacy
import pdfplumber
import docx
from flask import Flask, request, jsonify
import nltk
from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
from transformers import BertTokenizer, BertForSequenceClassification
import torch
import joblib
import os

app = Flask(__name__)

# Load NLP Model
nlp = spacy.load("en_core_web_md")
lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words('english'))

# Model paths
# Define the relative path to the saved_model directory
model_path = os.path.join(os.path.dirname(__file__),"TrainedModels","saved_model")
print("Model Path:", model_path)  # Debugging: Verify the path

# Load Trained BERT Model and Tokenizer
model = BertForSequenceClassification.from_pretrained(model_path)
tokenizer = BertTokenizer.from_pretrained(model_path)

# Load Label Encoder
label_encoder = joblib.load(os.path.join(os.path.dirname(__file__),"TrainedModels","label_encoder.pkl"))
def extract_text_from_file(file_bytes, file_type):
    if file_type == "pdf":
        with pdfplumber.open(file_bytes) as pdf:
            return "\n".join([page.extract_text() for page in pdf.pages if page.extract_text()])
    elif file_type == "docx":
        doc = docx.Document(file_bytes)
        return "\n".join([para.text for para in doc.paragraphs])
    else:
        return file_bytes.decode("utf-8")

def preprocess_text(text):
    text = re.sub(r'\W', ' ', text).lower()
    tokens = word_tokenize(text)
    tokens = [lemmatizer.lemmatize(word) for word in tokens if word not in stop_words]
    doc = nlp(text)
    entities = {ent.label_: ent.text for ent in doc.ents if ent.label_ in ["ORG", "GPE", "PERSON", "WORK_OF_ART"]}
    return " ".join(tokens), entities

def predict_category(text):
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True, max_length=512)
    with torch.no_grad():
        logits = model(**inputs).logits
    probabilities = torch.nn.functional.softmax(logits, dim=1)
    confidence = torch.max(probabilities).item() * 100
    predicted_label = torch.argmax(logits, dim=1).item()
    return label_encoder.inverse_transform([predicted_label])[0], confidence

def calculate_job_relevance(entities, required_skills, preferred_qualifications):
    resume_skills = [s.lower() for s in entities.get("WORK_OF_ART", "").split(", ")]
    required_skills = [s.lower() for s in required_skills]
    preferred_qualifications = [q.lower() for q in preferred_qualifications]

    required_match = len(set(resume_skills) & set(required_skills))
    preferred_match = len(set(resume_skills) & set(preferred_qualifications))

    relevance_score = (
        (required_match / max(len(required_skills), 1)) * 70 +
        (preferred_match / max(len(preferred_qualifications), 1)) * 30
    )
    return min(relevance_score, 100)  # Cap at 100%

@app.route('/screen_resume', methods=['POST'])
def screen_resume():
    try:
        file = request.files.get('resume')
        required_skills = request.form.getlist("required_skills[]")
        preferred_qualifications = request.form.getlist("preferred_qualifications[]")

        # File handling
        filename = file.filename.lower()
        if filename.endswith(".pdf"):
            file_type = "pdf"
        elif filename.endswith(".docx"):
            file_type = "docx"
        else:
            file_type = "txt"
        resume_text = extract_text_from_file(file, file_type)
        # Preprocessing
        processed_text, entities = preprocess_text(resume_text)

        # Prediction
        predicted_category, confidence = predict_category(processed_text)
        relevance_score = calculate_job_relevance(entities, required_skills, preferred_qualifications)
        
        # Final score calculation
        resume_score = int((confidence * 0.7) + (relevance_score * 0.3))

        return jsonify({
            "resume_score": resume_score,
            "predicted_category": predicted_category,
            "confidence": confidence,
            "relevance_score": relevance_score,
            "matched_skills": list(set(entities.get("WORK_OF_ART", "").split(", ")) & set(required_skills))
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/match_skills', methods=['POST'])
def match_skills():
    try:
        data = request.get_json()
        if not data or 'candidate_skills' not in data or 'job_skills' not in data:
            return jsonify({"error": "Missing candidate_skills or job_skills"}), 400

        candidate_skills = [skill.lower() for skill in data['candidate_skills']]
        job_skills = [skill.lower() for skill in data['job_skills']]

        # Calculate skill match score
        matched_skills = set(candidate_skills) & set(job_skills)
        skill_match_score = (len(matched_skills) / max(len(job_skills), 1)) * 100

        return jsonify({
            "skill_match_score": int(skill_match_score),
            "matched_skills": list(matched_skills)
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500
if __name__ == '__main__':
    app.run(debug=True, port=5001)