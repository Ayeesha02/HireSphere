# Recruitment Bias Mitigation Model
# This script trains a neural network model to predict hiring decisions and applies bias mitigation techniques.
# It uses the AIF360 library for bias detection and mitigation.

# Import necessary libraries
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import accuracy_score, classification_report
from sklearn.neural_network import MLPClassifier
import joblib
from aif360.datasets import BinaryLabelDataset
from aif360.metrics import ClassificationMetric
from aif360.algorithms.preprocessing import Reweighing
from aif360.algorithms.inprocessing import AdversarialDebiasing
import tensorflow as tf

# Data Processing: Load and preprocess the dataset
data_path = "data/hiring_dataset/recruitment_data.csv"
df = pd.read_csv(data_path)
df.fillna(df.median(), inplace=True)
df.columns = df.columns.str.strip()

# Add Age_above_40 column before scaling
df['Age_above_40'] = (df['Age'] > 40).astype(int)  # 1 if age > 40, 0 otherwise

# Encode categorical features
label_encoder = LabelEncoder()
df['Gender'] = label_encoder.fit_transform(df['Gender'])
df['RecruitmentStrategy'] = label_encoder.fit_transform(df['RecruitmentStrategy'])

# One-hot encoding for education level
df = pd.get_dummies(df, columns=['EducationLevel'], drop_first=True)

# Feature scaling for numerical features
scale_cols = ['Age', 'ExperienceYears', 'PreviousCompanies', 'DistanceFromCompany', 'InterviewScore', 'SkillScore', 'PersonalityScore']
scaler = StandardScaler()
df[scale_cols] = scaler.fit_transform(df[scale_cols])
joblib.dump(scaler, 'scaler.pkl')  

# Data preparation
# Define features and target variable
X = df[['Age', 'ExperienceYears', 'PreviousCompanies', 'DistanceFromCompany',
        'InterviewScore', 'SkillScore', 'PersonalityScore', 'Gender', 
        'RecruitmentStrategy', 'EducationLevel_2', 'EducationLevel_3', 'EducationLevel_4']]
y = df['HiringDecision']

# Split data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

# Convert to AIF360 dataset
privileged_groups = [{'Gender': 1}]  # Assuming 1 represents Male
unprivileged_groups = [{'Gender': 0}]  # Assuming 0 represents Female
df_train = pd.concat([X_train, y_train], axis=1)
df_train['Age_above_40'] = df['Age_above_40'].loc[X_train.index]
dataset = BinaryLabelDataset(df=df_train, label_names=['HiringDecision'], protected_attribute_names=['Gender', 'Age_above_40'])

# Apply Bias Mitigation (Reweighing pre-processing)
reweigher = Reweighing(unprivileged_groups=unprivileged_groups, privileged_groups=privileged_groups)
dataset_transf = reweigher.fit_transform(dataset)

# Model Training
# Train Neural Network
print("Training Neural Network...")
nn_model = MLPClassifier(hidden_layer_sizes=(16, 8), activation='relu', max_iter=1000, random_state=42)
nn_model.fit(X_train, y_train)

# Save the trained model
joblib.dump(nn_model, 'bias_model.pkl')

# Predictions
y_pred = nn_model.predict(X_test)
print("\nModel Accuracy:", accuracy_score(y_test, y_pred))
print("\nClassification Report:\n", classification_report(y_test, y_pred))

# Fairness Evaluation
df_test = pd.concat([X_test, y_test], axis=1)
df_test['Age_above_40'] = df['Age_above_40'].loc[X_test.index]
test_dataset = BinaryLabelDataset(df=df_test, label_names=['HiringDecision'], protected_attribute_names=['Gender', 'Age_above_40'])
test_pred_dataset = test_dataset.copy()
test_pred_dataset.labels = y_pred

# Evaluate fairness for gender
metric_gender = ClassificationMetric(test_dataset, test_pred_dataset, unprivileged_groups=unprivileged_groups, privileged_groups=privileged_groups)
print("\nDemographic Parity Difference for Gender:", metric_gender.mean_difference())
print("\nEqual Opportunity Difference for Gender:", metric_gender.equal_opportunity_difference())

# Evaluate fairness for Age_above_40
unprivileged_groups_age = [{'Age_above_40': 1}]  # Age > 40
privileged_groups_age = [{'Age_above_40': 0}]   # Age <= 40
metric_age = ClassificationMetric(test_dataset, test_pred_dataset, unprivileged_groups=unprivileged_groups_age, privileged_groups=privileged_groups_age)
print("\nDemographic Parity Difference for Age_above_40:", metric_age.mean_difference())
print("\nEqual Opportunity Difference for Age_above_40:", metric_age.equal_opportunity_difference())

# Applying Bias Mitigation using Adversarial Debiasing
print("\nApplying Bias Mitigation...")
tf.compat.v1.disable_eager_execution()
debias_model = AdversarialDebiasing(privileged_groups=privileged_groups, unprivileged_groups=unprivileged_groups, scope_name='debiased_nn', sess=tf.compat.v1.Session(), adversary_loss_weight=0.1)
debias_model.fit(dataset_transf)

# Mitigation Evaluation
y_pred_mitigated = debias_model.predict(test_dataset).labels
print("\nPost-Mitigation Accuracy:", accuracy_score(y_test, y_pred_mitigated))
print("\nPost-Mitigation Classification Report:\n", classification_report(y_test, y_pred_mitigated))

y_pred_mitigated_1d = pd.Series(y_pred_mitigated.ravel(), index=X_test.index, name="HiringDecision")

df_test_mitigated = pd.concat([X_test, y_pred_mitigated_1d], axis=1)
df_test_mitigated['Age_above_40'] = df['Age_above_40'].loc[X_test.index]
test_pred_mitigated_dataset = BinaryLabelDataset(
    df=df_test_mitigated,
    label_names=['HiringDecision'],
    protected_attribute_names=['Gender', 'Age_above_40']
)

# Evaluate Bias after Mitigation for gender
metric_mitigated_gender = ClassificationMetric(
    test_dataset,
    test_pred_mitigated_dataset,
    unprivileged_groups=unprivileged_groups,
    privileged_groups=privileged_groups
)
print("\nPost-Mitigation Demographic Parity Difference for Gender:", metric_mitigated_gender.mean_difference())
print("\nPost-Mitigation Equal Opportunity Difference for Gender:", metric_mitigated_gender.equal_opportunity_difference())

# Evaluate Bias after Mitigation for Age_above_40
metric_mitigated_age = ClassificationMetric(
    test_dataset,
    test_pred_mitigated_dataset,
    unprivileged_groups=unprivileged_groups_age,
    privileged_groups=privileged_groups_age
)
print("\nPost-Mitigation Demographic Parity Difference for Age_above_40:", metric_mitigated_age.mean_difference())
print("\nPost-Mitigation Equal Opportunity Difference for Age_above_40:", metric_mitigated_age.equal_opportunity_difference())