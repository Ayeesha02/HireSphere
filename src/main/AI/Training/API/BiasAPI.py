from flask import Flask, request, jsonify
import logging
import os
import pandas as pd
import joblib
from aif360.datasets import BinaryLabelDataset
from aif360.algorithms.preprocessing import Reweighing
from aif360.metrics import ClassificationMetric
import numpy as np

app = Flask(__name__)
#logging.basicConfig(level=logging.INFO)

# Load the trained model and scaler
model = joblib.load(os.path.join(os.path.dirname(__file__), "TrainedModels", "bias_model.pkl"))
scaler = joblib.load(os.path.join(os.path.dirname(__file__), "TrainedModels", "scaler.pkl"))

EXPECTED_COLUMNS = [
    'Age', 'ExperienceYears', 'PreviousCompanies', 'DistanceFromCompany',
    'InterviewScore', 'SkillScore', 'PersonalityScore', 'Gender', 
    'RecruitmentStrategy', 'EducationLevel_2', 'EducationLevel_3', 'EducationLevel_4'
]

def preprocess_candidate_data(candidate_data):
    feature_cols = [
        'Age', 'ExperienceYears', 'PreviousCompanies', 'DistanceFromCompany',
        'InterviewScore', 'SkillScore', 'PersonalityScore', 'Gender',
        'RecruitmentStrategy', 'EducationLevel'
    ] 
    df = pd.DataFrame([candidate_data])[feature_cols]
    gender_mapping = {"Male": 1, "Female": 0, "Unknown": -1}
    df['Gender'] = df['Gender'].map(gender_mapping).fillna(-1)
    strategy_mapping = {"Standard": 1, "Internal": 2, "Recommendation": 3}
    df['RecruitmentStrategy'] = df['RecruitmentStrategy'].map(strategy_mapping).fillna(0)
    df = pd.get_dummies(df, columns=['EducationLevel'], prefix='EducationLevel', drop_first=True)
    df['Age_above_40'] = (df['Age'] > 40).astype(int)  
    scale_cols = ['Age', 'ExperienceYears', 'PreviousCompanies', 'DistanceFromCompany', 
                  'InterviewScore', 'SkillScore', 'PersonalityScore']
    df[scale_cols] = scaler.transform(df[scale_cols])
    for col in EXPECTED_COLUMNS:
        if col not in df.columns:
            df[col] = 0
    df = df[EXPECTED_COLUMNS]
    return df

@app.route('/analyze_bias', methods=['POST'])
def analyze_bias(): 
    try:
        candidate_data = request.json.get("candidate_data")
        if not candidate_data:
            return jsonify({"error": "Missing candidate data"}), 400

        df = preprocess_candidate_data(candidate_data)  
        hiring_decision = candidate_data.get('HiringDecision', 0)
        df_with_target = df.copy()
        df_with_target['HiringDecision'] = hiring_decision 
        df_with_target['Age_above_40'] = (df_with_target['Age'] > 40).astype(int)  # Ensure Age_above_40 is present

        if 'HiringDecision' not in df_with_target.columns:
            return jsonify({"error": "HiringDecision column not added to DataFrame"}), 500

        dataset = BinaryLabelDataset(df=df_with_target, label_names=['HiringDecision'], protected_attribute_names=['Gender', 'Age_above_40'])
        
        # For gender
        privileged_groups_gender = [{'Gender': 1}]
        unprivileged_groups_gender = [{'Gender': 0}]
        reweigher = Reweighing(unprivileged_groups=unprivileged_groups_gender, privileged_groups=privileged_groups_gender)
        dataset_transf = reweigher.fit_transform(dataset)
        metric_gender = ClassificationMetric(dataset, dataset_transf, unprivileged_groups=unprivileged_groups_gender, privileged_groups=privileged_groups_gender)
        bias_score_gender = metric_gender.mean_difference()
        bias_score_gender = 0.0 if np.isnan(bias_score_gender) else bias_score_gender

        # For age (without reweighing, just compute metric on original vs. predicted)
        dataset_pred = dataset.copy()
        dataset_pred.labels = model.predict(df)
        metric_age = ClassificationMetric(dataset, dataset_pred, unprivileged_groups=[{'Age_above_40': 1}], privileged_groups=[{'Age_above_40': 0}])
        bias_score_age = metric_age.mean_difference()
        bias_score_age = 0.0 if np.isnan(bias_score_age) else bias_score_age

        prediction = model.predict(df)[0]

        # Determine if bias is detected 
        bias_detected = abs(bias_score_gender) > 0.8 or abs(bias_score_age) > 0.8

        return jsonify({
            "bias_score_gender": bias_score_gender,
            "bias_score_age": bias_score_age,
            "predicted_decision": int(prediction),
            "bias_detected": bias_detected,
            "message": "Bias analysis completed."
        })

    except Exception as e:
        logging.error(f"Error in bias analysis: {str(e)}")
        return jsonify({"error": str(e)}), 500   
    
if __name__ == '__main__':
    app.run(debug=True, port=5002)