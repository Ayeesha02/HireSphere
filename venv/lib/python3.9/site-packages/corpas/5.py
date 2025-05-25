import numpy as np
from sklearn.datasets import fetch_20newsgroups
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.svm import SVC
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split

newsgroups_train = fetch_20newsgroups(subset='train')

X_train, X_test, y_train, y_test = train_test_split(
    newsgroups_train.data, newsgroups_train.target, test_size=0.2, random_state=42
)

vectorizer = TfidfVectorizer()
X_train_vectorized = vectorizer.fit_transform(X_train)
X_test_vectorized = vectorizer.transform(X_test)

nb_classifier = MultinomialNB()
nb_classifier.fit(X_train_vectorized, y_train)
nb_predictions = nb_classifier.predict(X_test_vectorized)

svm_classifier = SVC(kernel='linear')
svm_classifier.fit(X_train_vectorized, y_train)
svm_predictions = svm_classifier.predict(X_test_vectorized)

print("Naïve Bayes Classifier:")
print(classification_report(y_test, nb_predictions, target_names=newsgroups_train.target_names))

print("\nSupport Vector Machine (SVM) Classifier:")
print(classification_report(y_test, svm_predictions, target_names=newsgroups_train.target_names))
