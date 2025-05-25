
### HireSphere - AI-Powered Hiring System

HireSphere is an AI-driven recruitment platform designed to improve hiring efficiency, fairness, and workforce planning. It automates resume screening, assesses candidates with an NLP chatbot, mitigates biases, and predicts staffing needs, tackling inefficiencies and inequities in traditional recruitment.

### Features

Automated Resume Screening: Uses BERT for contextual resume ranking.
AI Chatbot Assessment: Evaluates skills via text-based interviews.
Bias Detection: Employs AIF360 for real-time fairness auditing (gender, age).
Workforce Prediction: Applies LSTM for staffing forecasts.
Secure Access: JWT-based role authentication (Recruiter, Candidate, Admin).
Interface: React dashboards for HR and candidates.


### Technologies

Backend: Spring Boot, Spring Security (JWT, BCrypt)
AI: Flask, BERT, LSTM, AIF360, OpenRouter (in src/main/AI/)
Frontend: React, Chart.js
Database: PostgreSQL
Languages: Java, Python, JavaScript
Tools: Maven, npm, Gunicorn

### Prerequisites

Java 17+
Python 3.8+
Node.js 18+
PostgreSQL 15+

### Setup Instructions :

 ### Backend Setup


Navigate: inside the file 
Install Dependencies: mvn clean install
Configure Database: 
Create a PostgreSQL database: hiring_system
Update src/main/resources/application.properties:
	
	spring.datasource.url=jdbc:postgresql://localhost:5432/hiring_system
	spring.datasource.username=your_username
	spring.datasource.password=your_password
	spring.jpa.hibernate.ddl-auto=update

For better understanding of the databse, download Postgres and PgAdmin4

Run Spring Boot: mvn spring-boot:run or ./mvnw spring-boot:run
Access: http://localhost:8080

 ### AI Services Setup

The AI components (ResumeScreeningAPI.py, BiasAPI.py, WorkForcePredictionAPI.py and ChatbotAPI.py) are in  	src/main/AI/Training/API.

 ### Choose one method to run them:

### Method 1: Flask Built-In Server (Development)

1. Navigate: cd src/main/AI/Training/API.

2. Install Python Dependencies:

    pip install flask transformers tensorflow torch aif360

	    check the requirement.txt file for dependencies required

3. Run Each API in different terminal/ command prompt:

python3 ResumeScreeningAPI.py  		    #port 5001
python3 BiasAPI.py				         #port 5002
python3  WorkForcePredictionAPI.py 		  #port 5000
python3 ChatbotAPI.py			        #port 5004

	while running the API make sure no other device or component is active on the API’s port

### Method 2: Gunicorn (Production/Testing)

1.Install Gunicorn:

cd src/main/AI/Training/API
pip install gunicorn

2.Run each API in separate terminal:

gunicorn --bind 0.0.0.0:5001 ResumeScreeningAPI:app
gunicorn --bind 0.0.0.0:5002 BiasAPI.:app
gunicorn --bind 0.0.0.0:5000 WorkForcePredictionAPI:app
gunicorn --bind 0.0.0.0:5004 ChatbotAPI:app

 ### Front End Setup:
	
Navigate: cd hiringsystem-frontend
Install Dependencies: npm install
Run: npm start
Access: http://localhost:3000


### Database Initialization

Run database/init.sql to set up tables (e.g., users, jobs). Or Create `hiring_system` in PostgreSQL; schema auto-generates via Hibernate

### Usage

Register/Login:

Recruiter: POST /recruiters/register/recruiter, /recruiters/login
Candidate: POST /candidates/register, /candidates/login

Admin: POST /admin/register, /admin/login
Recruiter: Post jobs, screen resumes, view predictions (/recruiters).
Candidate: Apply to jobs , complete chatbot interview.(/candidates).
Admin: get audit logs (/admin).


### Notes

Security: JWT secures endpoints; passwords use BCrypt.
Deployment: Local setup; Gunicorn enhances Flask performance.
AI Models: In src/main/AI/—ensure model weights (e.g., BERT) are included.

### Troubleshooting

CORS: Check localhost:3000 in SecurityConfig.java.
API Errors: Verify Flask/Gunicorn ports and model paths.
Auth: Ensure UserDetailService hashes passwords.
If you are running multiple tabs of hiring system in your browser, I highly  recommend you to run it in incognitive mode.
If you are facing any issue during the login or after logging, Log out from yout account, close all server connections (maven, react) and re-run it. Of the issue still persist, clear

### Future Enhancements

Systemd for Gunicorn automation.
Cloud deployment (e.g., AWS).
Frontend for admin in a different port than the candidate and recruiter

### Dataset 
These are dataset that were used in their training process. The saved models are saved under src/main/AI/Training/API/TrainedModels
The training model is placed src/main/AI/Training

1. Bias Model: https://www.kaggle.com/datasets/rabieelkharoua/predicting-hiring-decisions-in-recruitment-data
2. Resume Screenning Model: https://www.kaggle.com/datasets/snehaanbhawal/resume-dataset
3. Work Force Prediction Model: https://www.kaggle.com/datasets/ravindrasinghrana/employeedataset (the employee dataset was used)

These dataset where placed under a folder named data under src/main/AI/Training


### Author

Ayeesha Ubedulla Shariif






