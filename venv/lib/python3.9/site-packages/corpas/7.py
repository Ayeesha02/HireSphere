import nltk
from nltk.tokenize import word_tokenize
from nltk.tag import pos_tag
from nltk.corpus import stopwords

nltk.download('punkt')
nltk.download('averaged_perceptron_tagger_eng')
nltk.download('stopwords')

text = "John likes to play football with his friends."
tokens = word_tokenize(text)

def rule_based_pos_tagging(tokens):
    return [
        (token, 'NNP') if token.lower() in ["john", "he", "his"] else
        (token, 'VB') if token.lower() in ["likes", "play"] else
        (token, 'TO') if token.lower() in ["to", "with"] else
        (token, 'NN')
        for token in tokens
    ]

stop_words = set(stopwords.words('english'))
tokens_without_stopwords = [token for token in tokens if token.lower() not in stop_words]

print("Rule-based PoS tagging:")
print(rule_based_pos_tagging(tokens))

print("\nStatistical PoS tagging:")
print(pos_tag(tokens_without_stopwords))
