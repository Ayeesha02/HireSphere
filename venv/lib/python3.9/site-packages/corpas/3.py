import numpy as np
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer
from gensim.models import Word2Vec

text_data = [
    "This is the first document.",
    "This document is the second document.",
    "And this is the third one.",
    "Is this the first document?",
]

one_hot_vectorizer = CountVectorizer(binary=True)
one_hot_encoded = one_hot_vectorizer.fit_transform(text_data).toarray()

print("One-Hot Encoding:")
print(one_hot_encoded)

bow_vectorizer = CountVectorizer()
bow_features = bow_vectorizer.fit_transform(text_data).toarray()

print("\nBag of Words (BoW):")
print(bow_features)

ngram_vectorizer = CountVectorizer(ngram_range=(1, 2))
ngram_features = ngram_vectorizer.fit_transform(text_data).toarray()

print("\nn-grams:")
print(ngram_features)

tfidf_vectorizer = TfidfVectorizer()
tfidf_features = tfidf_vectorizer.fit_transform(text_data).toarray()

print("\nTF-IDF:")
print(tfidf_features)

custom_features = np.array([[len(doc)] for doc in text_data])

print("\nCustom Features (Document Length):")
print(custom_features)

word2vec_model = Word2Vec(
    [doc.split() for doc in text_data], 
    min_count=1, 
    vector_size=10
)

word2vec_features = np.array([
    np.mean(
        [
            word2vec_model.wv.get_vector(word) 
            for word in doc.split() 
            if word in word2vec_model.wv
        ], 
        axis=0
    )
    for doc in text_data
])

print("\nWord2Vec Features:")
print(word2vec_features)
