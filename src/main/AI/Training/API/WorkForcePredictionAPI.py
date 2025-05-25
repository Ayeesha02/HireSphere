from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
from tensorflow.keras.models import load_model
import tensorflow.keras.backend as K
import joblib
import os
import logging

app = Flask(__name__)

# Set up logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# Load the Keras model and Joblib scaler with correct paths
model_path = os.path.join(os.path.dirname(__file__), "TrainedModels", "workforce_model.h5")
scaler_path = os.path.join(os.path.dirname(__file__), "TrainedModels", "scaler_wfp.pkl")

def mse(y_true, y_pred):
    return K.mean(K.square(y_pred - y_true), axis=-1)

def mae(y_true, y_pred):
    return K.mean(K.abs(y_pred - y_true), axis=-1)

try:
    model = load_model(model_path, custom_objects={'mse': mse, 'mae': mae})
    scaler = joblib.load(scaler_path)
except Exception as e:
    logger.error(f"Failed to load model or scaler: {e}")
    raise

FEATURES = ['Performance Score', 'employment_duration']

# Load and preprocess Dataset
def load_dataset(file):
    logger.debug("Reading CSV file")
    df = pd.read_csv(file)
    logger.debug(f"Columns in CSV: {df.columns.tolist()}")

    logger.debug("Parsing StartDate")
    df['StartDate'] = pd.to_datetime(df['StartDate'], format='%d-%b-%y', errors='coerce')
    logger.debug("Parsing ExitDate")
    df['ExitDate'] = pd.to_datetime(df['ExitDate'].fillna('2100-01-01'), format='%d-%b-%y', errors='coerce')
    
    logger.debug("Calculating employment_duration")
    df['employment_duration'] = (df['ExitDate'] - df['StartDate']).dt.days
    df['is_active'] = (df['ExitDate'].dt.year == 2100).astype(int)
    df['year_month'] = df['StartDate'].dt.to_period('M').dt.to_timestamp()
    
    perf_map = {'Excellent': 5, 'Satisfactory': 3, 'Needs Improvement': 1}
    df['Performance Score'] = df['Performance Score'].map(lambda x: perf_map.get(x, 3))
    
    df['employment_duration'] = df['employment_duration'].fillna(0)
    
    return df

# Prepare sequences for LSTM
def prepare_sequences(df, lookback=12):
    logger.debug("Aggregating monthly data")
    monthly_data = df.groupby('year_month').agg({
        'EmpID': 'count',
        'is_active': lambda x: (x == 0).sum(),
        'Performance Score': 'mean',
        'employment_duration': 'mean'
    }).reset_index()
    
    monthly_data = monthly_data.sort_values('year_month')
    
    features = ['Performance Score', 'employment_duration']
    X = []
    
    for i in range(lookback, len(monthly_data)):
        X.append(monthly_data[features].iloc[i-lookback:i].values)
    
    return np.array(X)

# Predict skills demand
def predict_skills_demand(df):
    try:
        common_skills = [
            'Java', 'Python', 'SQL', 'AWS', 'JavaScript', 'C++', 'C#', 'Ruby', 'PHP', 'React',
            'Angular', 'Node.js', 'Docker', 'Kubernetes', 'Machine Learning', 'Data Analysis',
            'Cloud Computing', 'DevOps', 'Linux', 'Networking', 'Cybersecurity', 'HTML', 'CSS',
            'Git', 'Agile', 'Scrum', 'Project Management', 'Business Analysis', 'UI/UX Design',
            'Strategic Planning', 'Financial Acumen', 'Stakeholder Management','Corporate Governance',
            'Mergers & Acquisitions', 'Change Management','Crisis Leadership','Business Model Innovation', 
            'KPI Development','ESG Strategy', 'Board Communication', 'Global Market Analysis',
            'Talent Acquisition', 'Succession Planning', 'Compensation Design','HRIS (Workday/SAP SuccessFactors)', 
            'Employee Engagement', 'Labor Law', 'Diversity & Inclusion', 'Performance Management',
            'Payroll Systems', 'Conflict Resolution','Learning Management Systems (LMS)', 'HR Analytics', 'Budget Forecasting',
            'Tax Compliance', 'Auditing', 'Cost Accounting', 'ERP Systems (SAP, Oracle)', 'Excel Advanced Modeling',
            'Power BI/Tableau', 'Blockchain Accounting','Vendor Management', 'Disaster Recovery Planning',
            'IT Governance', 'Tech Support Troubleshooting', 'Network Security','FP&A', 'Variance Analysis', 
            'AP/AR Management', 'Risk Modeling', 'SEO/SEM (Google Analytics, Ahrefs)', 'Content Marketing',
            'Social Media Strategy (Hootsuite, Sprout Social)','Brand Positioning', 'Marketing Automation (HubSpot, Marketo)',
            'Customer Segmentation', 'A/B Testing', 'CRM Campaigns','Influencer Partnerships', 'Data-Driven Storytelling', 
            'CRM Systems (Zendesk, Freshdesk)','Customer Journey Mapping', 'Ticket Resolution',
            'Empathy Training', 'Upselling/Cross-Selling', 'SLA Management', 'Voice of Customer (VoC) Analysis',
            'Knowledge Base Development', 'Chatbot Configuration', 'Lean Six Sigma', 'Inventory Optimization', 'ERP Implementation',
            'Logistics Management (SAP WM, WMS)', 'Vendor Negotiation','Process Automation (RPA)', 'Quality Assurance',
            'Demand Forecasting', 'SCOR Model', 'ISO Compliance', 'Supply Chain Risk Management']

        
        text_data = df['Title'].fillna('') + ' ' + df['Job Function'].fillna('')
        text_data = text_data.str.lower()
        
        demand = {}
        total_entries = len(text_data)
        
        logger.debug(f"Processing skills demand for {len(common_skills)} skills")
        for skill in common_skills:
            skill_lower = skill.lower()
            demand[skill] = text_data.str.contains(skill_lower, na=False, regex=False).sum() / total_entries

        
            demand = {k: round(min(v, 1.0), 2) for k, v in demand.items() if v > 0}

        return demand
    except Exception as e:
        logger.error(f"Error in predict_skills_demand: {e}")
        raise

@app.route('/predict', methods=['POST'])
def predict():
    try:
        logger.debug("Received predict request")
        file = request.files['file']
        df = load_dataset(file)
        
        logger.debug(f"Dataframe shape: {df.shape}")
        if not all(col in df.columns for col in FEATURES):
            return jsonify({"error": "Missing required features"}), 400
        
        df[FEATURES] = scaler.transform(df[FEATURES])
        X = prepare_sequences(df)
        
        if X.shape[0] == 0:
            return jsonify({"error": "Insufficient data for prediction (need at least 12 months)"}), 400
        
        logger.debug("Making prediction with model")
        turnover_pred, hires_pred = model.predict(X)
        
        skills_demand = predict_skills_demand(df)
        
        return jsonify({
            "predicted_turnover": int(turnover_pred[-1][0]),
            "predicted_hires": int(hires_pred[-1][0]),
            "skills_demand": skills_demand
        })
    except Exception as e:
        logger.error(f"Prediction failed: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000)