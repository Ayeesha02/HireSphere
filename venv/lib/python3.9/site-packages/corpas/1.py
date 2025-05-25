import nltk
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer, PorterStemmer
from nltk.corpus import stopwords
import string

nltk.download('punkt_tab')
nltk.download('wordnet')
nltk.download('stopwords')

lemmatizer = WordNetLemmatizer()
stemmer = PorterStemmer()
stop_words = set(stopwords.words('english'))
punctuations = set(string.punctuation)

def preprocess_text(text):
    tokens = [token for token in word_tokenize(text.lower()) if token not in punctuations and token not in stop_words]
    return tokens

def lemmatize(tokens):
    return [lemmatizer.lemmatize(token) for token in tokens]

def stem(tokens):
    return [stemmer.stem(token) for token in tokens]

def main():
    text = "Tokenization is the process of breaking down text into words and phrases. Stemming and Lemmatization are techniques used to reduce words to their base form."
    tokens = preprocess_text(text)
    
    print("Lemmatization:", lemmatize(tokens))
    print("\nStemming:", stem(tokens))

if __name__ == "__main__":  
    main()
