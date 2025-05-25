#Resume Screening Model
# This script is designed to preprocess resumes, extract relevant information, and train a BERT model for resume classification.
# It uses the Hugging Face Transformers library for BERT and Spacy for NLP tasks.

# Import necessary libraries
import re
import nltk
import spacy
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer
import pandas as pd
from transformers import BertTokenizer
import torch
from transformers import BertForSequenceClassification, Trainer, TrainingArguments
from sklearn.metrics import accuracy_score, classification_report
import joblib
nltk.download('punkt')
nltk.download('stopwords')
nltk.download('wordnet')

#NLP Preprocessing
# Load NLP Model
nlp = spacy.load("en_core_web_sm")

# Initialize NLTK tools
lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words('english'))

#Text Preprocessing Function
def preprocess_text(text):

    # Remove special characters
    text = re.sub(r'\W', ' ', text)
    text = re.sub(r'\s+', ' ', text)
    text = text.lower()

    # Tokenization and Lemmatization
    tokens = word_tokenize(text)
    tokens = [lemmatizer.lemmatize(word) for word in tokens if word not in stop_words]

    # Extract Skills, Experience, Education
    doc = nlp(text)
    entities = {ent.label_: ent.text for ent in doc.ents if ent.label_ in ["ORG", "WORK_OF_ART", "GPE", "PERSON"]}

    return " ".join(tokens), entities  # Returning processed text + extracted entities

#Data Preparation
# Load the Resume dataset
data_resume_path = "data/resume_dataset/archive/Resume/Resume.csv"
df = pd.read_csv(data_resume_path)

# Applying Preprocessing
df['Cleaned_Resume'], df['Resume_Entities'] = zip(*df['Resume_str'].apply(preprocess_text))

# Label Encoding
from sklearn.preprocessing import LabelEncoder
label_encoder = LabelEncoder()
df['Category_Label'] = label_encoder.fit_transform(df['Category']) 

# Split the dataset
from sklearn.model_selection import train_test_split

X_train, X_test, y_train, y_test = train_test_split(df['Cleaned_Resume'], df['Category_Label'], 
                                                    test_size=0.2, random_state=42, stratify=df['Category_Label'])

#BERT configuration
# Load BERT tokenizer
tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')

# Tokenize the text
train_encodings = tokenizer(X_train.tolist(), truncation=True, padding=True, max_length=512, return_tensors='pt')
test_encodings = tokenizer(X_test.tolist(), truncation=True, padding=True, max_length=512, return_tensors='pt')

# Convert labels to tensors
train_labels = torch.tensor(y_train.tolist())
test_labels = torch.tensor(y_test.tolist())

# Convert to Dataset format
class ResumeDataset(torch.utils.data.Dataset):
    def __init__(self, encodings, labels):
        self.encodings = encodings
        self.labels = labels

    def __len__(self):
        return len(self.labels)

    def __getitem__(self, idx):
        item = {key: val[idx] for key, val in self.encodings.items()}
        label = self.labels[idx]
        return {
            'input_ids': item['input_ids'],
            'attention_mask': item['attention_mask'],
            'label': label
        }

# Create Dataset
train_dataset = ResumeDataset(train_encodings, train_labels)
test_dataset = ResumeDataset(test_encodings, test_labels)

#BERT Model Training
# Load BERT Model
model = BertForSequenceClassification.from_pretrained('bert-base-uncased', num_labels=len(label_encoder.classes_))
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)

# Training Arguments
training_args = TrainingArguments(
    output_dir="./results",
    evaluation_strategy="epoch",
    save_strategy="epoch",
    learning_rate=2e-5,
    per_device_train_batch_size=16,
    per_device_eval_batch_size=16,
    num_train_epochs=5,  # Increase for better results
    weight_decay=0.01,
    logging_dir="./logs",
)

# Trainer
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=train_dataset,
    eval_dataset=test_dataset,
)

# Train Model
trainer.train()

# Evaluate Model
# Predict on test set
trainer.eval_dataset = test_dataset
predictions = trainer.predict(test_dataset)
y_pred = torch.argmax(torch.tensor(predictions.predictions), axis=1).tolist()

# Print evaluation results
print("Accuracy:", accuracy_score(y_test, y_pred))
print(classification_report(y_test, y_pred, target_names=label_encoder.classes_))

# Save the model
model.save_pretrained("saved_model")
tokenizer.save_pretrained("saved_model")
joblib.dump(label_encoder, "label_encoder.pkl")   

