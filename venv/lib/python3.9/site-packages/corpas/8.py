import numpy as np
import random
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense
from tensorflow.keras.callbacks import LambdaCallback

with open("text_corpus.txt", "r", encoding="utf-8") as f:
    text = f.read().lower()

chars = sorted(set(text))
char_indices = {char: i for i, char in enumerate(chars)}
indices_char = {i: char for i, char in enumerate(chars)}

max_len = 40
step = 3
sentences = [text[i : i + max_len] for i in range(0, len(text) - max_len, step)]
next_chars = [text[i + max_len] for i in range(0, len(text) - max_len, step)]

x = np.zeros((len(sentences), max_len, len(chars)), dtype=np.float32)
y = np.zeros((len(sentences), len(chars)), dtype=np.float32)

for i, sentence in enumerate(sentences):
    for t, char in enumerate(sentence):
        x[i, t, char_indices.get(char, 0)] = 1.0
    y[i, char_indices.get(next_chars[i], 0)] = 1.0

model = Sequential(
    [
        LSTM(128, input_shape=(max_len, len(chars))),
        Dense(len(chars), activation="softmax"),
    ]
)
model.compile(loss="categorical_crossentropy", optimizer="adam")

def sample(preds, temperature=1.0):
    preds = np.log(preds + 1e-10) / temperature
    preds = np.exp(preds) / np.sum(np.exp(preds))
    return np.random.choice(len(preds), p=preds)

def generate_text(seed_text, temperature=0.5, length=400):
    generated_text = seed_text
    seed_text = seed_text.lower()[-max_len:]

    for _ in range(length):
        x_pred = np.zeros((1, max_len, len(chars)), dtype=np.float32)
        for t, char in enumerate(seed_text):
            x_pred[0, t, char_indices.get(char, 0)] = 1.0

        preds = model.predict(x_pred, verbose=0)[0]
        next_char = indices_char[sample(preds, temperature)]

        generated_text += next_char
        seed_text = seed_text[1:] + next_char

    return generated_text

def on_epoch_end(epoch, _):
    print(f"\n----- Generating text after Epoch: {epoch + 1}")
    start_index = random.randint(0, len(text) - max_len - 1)
    seed_text = text[start_index : start_index + max_len]
    
    for temperature in [0.2, 0.5, 1.0]:
        print(f"----- Temperature: {temperature}")
        print(generate_text(seed_text, temperature))

model.fit(x, y, batch_size=128, epochs=30, callbacks=[LambdaCallback(on_epoch_end=on_epoch_end)])
