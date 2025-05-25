import numpy as np
from sklearn.decomposition import TruncatedSVD
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.preprocessing import normalize

documents = [
    "baseball soccer basketball",
    "soccer basketball tennis",
    "tennis cricket",
    "cricket soccer"
]

vectorizer = CountVectorizer()
X = vectorizer.fit_transform(documents)

lsa = TruncatedSVD(n_components=2)
lsa_components = lsa.fit_transform(X)

topic_matrix = normalize(lsa.components_)
terms = vectorizer.get_feature_names_out()

print("Top terms for each topic:")
for i, topic in enumerate(topic_matrix):
    top_indices = topic.argsort()[-min(len(terms), 5):][::-1]
    top_terms = [terms[idx] for idx in top_indices]
    print(f"Topic {i + 1}: {' '.join(top_terms)}")
