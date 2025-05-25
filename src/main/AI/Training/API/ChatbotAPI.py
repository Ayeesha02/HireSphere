import nltk
import logging
import requests
from flask import Flask, request, jsonify
from nltk.sentiment import SentimentIntensityAnalyzer
from sentence_transformers import SentenceTransformer, util

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

nltk.download('vader_lexicon')
sentiment_analyzer = SentimentIntensityAnalyzer()
bert_model = SentenceTransformer('all-MiniLM-L6-v2')

OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
API_TOKEN = "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"
HEADERS = {"Authorization": f"Bearer {API_TOKEN}"}

def generate_text(prompt):
    try:
        payload = {
            "model": "google/gemma-3-1b-it:free",
            "messages": [
                {"role": "system", "content": "You are an Interviewer. Generate only the question text, without conversational phrases like 'Created,' 'Okay,' or 'Here’s.'"},
                {"role": "user", "content": prompt}
            ],
            "temperature": 0.3,
            "max_tokens": 150,
            "top_p": 0.9
        }
        
        response = requests.post(OPENROUTER_URL, headers=HEADERS, json=payload, timeout=10)
        response.raise_for_status()
        response_data = response.json()
        logger.info(f"Raw OpenRouter response: {response_data}")
        
        if 'choices' not in response_data or len(response_data['choices']) == 0:
            raise ValueError("Invalid response format from API")

        generated_text = response_data['choices'][0]['message']['content'].strip()
        logger.info(f"Generated text before cleaning: {generated_text}")

        unwanted_phrases = ["Created", "Created.", "Okay", "Okay,", "Here’s", "Question:", "Sure,"]
        for phrase in unwanted_phrases:
            if generated_text.startswith(phrase):
                generated_text = generated_text[len(phrase):].strip()
        
        if generated_text.startswith(prompt):
            generated_text = generated_text[len(prompt):].strip()

        generated_text = generated_text.replace("okay", "").replace("here’s", "").strip()
        if generated_text.startswith("."):
            generated_text = generated_text[1:].strip()  # Remove stray "."

        if not generated_text.endswith("?"):
            generated_text += "?"
        
        logger.info(f"Cleaned question: {generated_text}")
        return generated_text
    except requests.exceptions.RequestException as e:
        logger.error(f"API request failed: {str(e)}")
        raise RuntimeError("Service temporarily unavailable")
    except (KeyError, ValueError) as e:
        logger.error(f"Response parsing failed: {str(e)}")
        raise RuntimeError("Invalid API response")

TECHNICAL_PROMPTS = [
    lambda job_title, required_skills, _: (
        f"Generate a basic technical question for a {job_title} position requiring skills in {', '.join(required_skills)}. Start with 'How' or 'What'."
    ),
    lambda job_title, required_skills, _: (
        f"Generate one skill-specific technical question for a {job_title} position focusing on one of these skills: {', '.join(required_skills)}. Start with 'How' or 'What'."
    ),
    lambda job_title, _, preferred_qualifications: (
        f"Generate a technical interview question for a {job_title} position related to one of these preferred qualifications: {', '.join(preferred_qualifications)}. Start with 'How' or 'What'."
    )
]

BEHAVIORAL_PROMPTS = [
    lambda job_title, required_skills: (
        f"Generate one behavioral interview question for a {job_title} position about teamwork, relevant to skills: {', '.join(required_skills)}. Start with 'How would you,' 'What are the,' or 'Tell me about a time when'."
    ),
    lambda job_title, required_skills: (
        f"Generate one behavioral interview question for a {job_title} position about time management, relevant to skills: {', '.join(required_skills)}. Start with 'How would you,' 'What are the,' or 'Tell me about a time when'."
    )
]

def generate_interview_question(job_title, required_skills, preferred_qualifications, technical_count):
    prompt_idx = min(technical_count, 2)
    prompt = TECHNICAL_PROMPTS[prompt_idx](job_title, required_skills, preferred_qualifications)
    return generate_text(prompt)

def generate_behavioral_question(job_title, required_skills, behavioral_count):
    prompt_idx = min(behavioral_count, 1)
    prompt = BEHAVIORAL_PROMPTS[prompt_idx](job_title, required_skills)
    return generate_text(prompt)

def generate_expected_answer(question):
    prompt = f"Provide a concise ideal answer for this interview question: {question}"
    return generate_text(prompt)

def evaluate_response(candidate_response, expected_answer):
    sentiment_score = sentiment_analyzer.polarity_scores(candidate_response)['compound']
    candidate_embedding = bert_model.encode(candidate_response, convert_to_tensor=True)
    expected_embedding = bert_model.encode(expected_answer, convert_to_tensor=True)
    similarity_score = util.pytorch_cos_sim(candidate_embedding, expected_embedding).item()
    overall_score = (sentiment_score + 1) / 2 * 50 + similarity_score * 50
    return {
        "sentiment_score": sentiment_score,
        "similarity_score": similarity_score,
        "overall_score": overall_score
    }

@app.route('/generate_question', methods=['POST'])
def generate_question_endpoint():
    try:
        data = request.json
        job_title = data.get("job_title")
        required_skills = data.get("required_skills", [])
        preferred_qualifications = data.get("preferred_qualifications", [])
        technical_count = data.get("technical_count", 0)
        behavioral_count = data.get("behavioral_count", 0)

        if not job_title:
            return jsonify({"error": "Missing job title"}), 400

        if technical_count < 3:
            question_type = "technical"
            question = generate_interview_question(job_title, required_skills, preferred_qualifications, technical_count)
        elif behavioral_count < 2:
            question_type = "behavioral"
            question = generate_behavioral_question(job_title, required_skills, behavioral_count)
        else:
            return jsonify({"message": "Interview completed"}), 200

        return jsonify({"question": question, "question_type": question_type})
    except Exception as e:
        logger.error(f"Error in generate_question_endpoint: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/conduct_interview', methods=['POST'])
def conduct_interview():
    try:
        data = request.json
        candidate_response = data.get("response")
        question = data.get("question")
        
        if not candidate_response or not question:
            return jsonify({"error": "Missing response or question"}), 400

        expected_answer = generate_expected_answer(question)
        scores = evaluate_response(candidate_response, expected_answer)
        
        question_type = data.get("question_type", "technical" if "how" in question.lower() else "behavioral")
        
        return jsonify({
            "question": question,
            "expected_answer": expected_answer,
            "candidate_response": candidate_response,
            "sentiment_score": scores["sentiment_score"],
            "similarity_score": scores["similarity_score"],
            "overall_score": scores["overall_score"],
            "question_type": question_type
        })
    except Exception as e:
        logger.error(f"Error in conduct_interview: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5004)