#Work Force Prediction Model
#This model predicts workforce metrics like turnover and hires based on employee data.
# It uses LSTM for time series forecasting.

# Import necessary libraries
import pandas as pd
import numpy as np
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input, LSTM, Dense
from sklearn.preprocessing import MinMaxScaler
import joblib

# Load and preprocessing dataset
def load_dataset(dataset_path):
    df = pd.read_csv(dataset_path)
    df['StartDate'] = pd.to_datetime(df['StartDate'], format='%d-%b-%y', errors='coerce')
    df['ExitDate'] = pd.to_datetime(df['ExitDate'].fillna('2100-01-01'), format='%d-%b-%y', errors='coerce')
    
    # Feature creation
    df['employment_duration'] = (df['ExitDate'] - df['StartDate']).dt.days
    df['is_active'] = (df['ExitDate'].dt.year == 2100).astype(int)  # 1 if active, 0 if left
    df['year_month'] = df['StartDate'].dt.to_period('M').dt.to_timestamp()
    
    # Convert Performance Score to numeric
    perf_map = {'Excellent': 5, 'Satisfactory': 3, 'Needs Improvement': 1}
    df['Performance Score'] = df['Performance Score'].map(lambda x: perf_map.get(x, 3))
    
    # Handle NaN from date parsing
    df['employment_duration'] = df['employment_duration'].fillna(0)
    
    return df

# Prepare sequences for LSTM
def prepare_sequences(df, lookback=12):
    monthly_data = df.groupby('year_month').agg({
        'EmpID': 'count',  # Hires
        'is_active': lambda x: (x == 0).sum(),  # Turnover (count of non-active)
        'Performance Score': 'mean',
        'employment_duration': 'mean'
    }).reset_index()
    
    monthly_data = monthly_data.sort_values('year_month')
    
    features = ['Performance Score', 'employment_duration']
    X, y_turnover, y_hires = [], [], []
    
    for i in range(lookback, len(monthly_data)):
        X.append(monthly_data[features].iloc[i-lookback:i].values)
        y_turnover.append(monthly_data['is_active'].iloc[i])  # Turnover count
        y_hires.append(monthly_data['EmpID'].iloc[i])   # Hires count
    
    return np.array(X), np.array(y_turnover), np.array(y_hires)

# Train the model
def train_model(csv_path):
    df = load_dataset(csv_path)
    
    scaler = MinMaxScaler()
    features = ['Performance Score', 'employment_duration']
    df[features] = scaler.fit_transform(df[features])
    
    X, y_turnover, y_hires = prepare_sequences(df)
    
    if X.shape[0] == 0:
        raise ValueError("Insufficient data for training after sequence preparation")
    
    input_layer = Input(shape=(12, len(features)))
    lstm = LSTM(64, return_sequences=True)(input_layer)
    lstm = LSTM(32)(lstm)
    
    turnover_output = Dense(1, activation='relu', name='turnover')(lstm)
    hires_output = Dense(1, activation='relu', name='hires')(lstm)
    
    model = Model(inputs=input_layer, outputs=[turnover_output, hires_output])
    model.compile(
        optimizer='adam',
        loss={'turnover': 'mse', 'hires': 'mse'},
        metrics={'turnover': 'mae', 'hires': 'mae'}
    )
    
    model.fit(X, {'turnover': y_turnover, 'hires': y_hires}, epochs=20, batch_size=32, validation_split=0.2)
    
    # Save model
    model.save('workforce_model.h5')
    joblib.dump(scaler, 'scaler_wfp.pkl')  # Match API scaler name
    print("Model and scaler saved successfully.")

if __name__ == '__main__':
    dataset_path = "data/Employee/archive (No.2)/employee_data.csv"
    train_model(dataset_path)