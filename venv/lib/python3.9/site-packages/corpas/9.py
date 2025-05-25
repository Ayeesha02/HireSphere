import numpy as np
from hmmlearn import hmm
from sklearn_crfsuite import CRF
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report

X = [['walk', 'in', 'the', 'park'],
    ['eat', 'apple'],
    ['eat', 'apple', 'in', 'the', 'morning']]

y = [['V', 'P', 'D', 'N'],
    ['V', 'N'],
    ['V', 'N', 'P', 'D', 'N']]

word_to_idx = {
    word: i for i, word in enumerate({word for sentence in X for word in sentence})
}

X_numeric = [
    [word_to_idx[word] for word in sentence] for sentence in X
]

X_train, X_test, y_train, y_test = train_test_split(
    X_numeric, y, test_size=0.2, random_state=42
)

hmm_model = hmm.MultinomialHMM(n_components=3, n_iter=100, random_state=42)

X_train_flat = np.concatenate(X_train).reshape(-1, 1)
lengths_train = list(map(len, X_train))

tag_to_idx = {
    tag: i for i, tag in enumerate({tag for tags in y for tag in tags})
}

y_train_flat = np.array([
    tag_to_idx[tag] for tags in y_train for tag in tags
])

hmm_model.fit(X_train_flat, lengths_train)

X_test_flat = np.concatenate(X_test).reshape(-1, 1)
lengths_test = list(map(len, X_test))

hmm_pred = [
    tag for idx in hmm_model.predict(X_test_flat, lengths_test)
    for tag in tag_to_idx if tag_to_idx[tag] == idx
]

X_train_crf = [
    [{'word': word} for word in sentence] for sentence in X_train
]

X_test_crf = [
    [{'word': word} for word in sentence] for sentence in X_test
]

crf_model = CRF()
crf_model.fit(X_train_crf, y_train)

crf_pred = [
    tag for tags in crf_model.predict(X_test_crf) for tag in tags
]

print("HMM Results:")
print(classification_report(
    [tag for tags in y_test for tag in tags], hmm_pred
))

print("\nCRF Results:")
print(classification_report(
    [tag for tags in y_test for tag in tags], crf_pred
))
