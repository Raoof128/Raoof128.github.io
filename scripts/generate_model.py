#!/usr/bin/env python3
"""
Mehr Guard ML Model Training Script

Trains a simple logistic regression model for phishing URL detection.
This is a reference implementation - the actual model runs in Kotlin.
"""

import json
import numpy as np
from typing import List, Tuple
import re
from math import log2

# Feature extraction functions
def extract_features(url: str) -> np.ndarray:
    """Extract 15 features from a URL."""
    features = np.zeros(15, dtype=np.float32)
    
    # Normalize URL
    protocol = "https" if url.startswith("https://") else "http"
    clean_url = url.replace("https://", "").replace("http://", "")
    
    host_end = min(
        clean_url.find("/") if "/" in clean_url else len(clean_url),
        clean_url.find("?") if "?" in clean_url else len(clean_url)
    )
    host = clean_url[:host_end]
    path_and_query = clean_url[host_end:]
    path = path_and_query.split("?")[0] if "?" in path_and_query else path_and_query
    
    # Feature 0: URL length (normalized, max 500)
    features[0] = min(len(url) / 500.0, 1.0)
    
    # Feature 1: Host length (normalized, max 100)
    features[1] = min(len(host) / 100.0, 1.0)
    
    # Feature 2: Path length (normalized, max 200)
    features[2] = min(len(path) / 200.0, 1.0)
    
    # Feature 3: Subdomain count (normalized, max 5)
    subdomain_count = max(host.count(".") - 1, 0)
    features[3] = min(subdomain_count / 5.0, 1.0)
    
    # Feature 4: Has HTTPS
    features[4] = 1.0 if protocol == "https" else 0.0
    
    # Feature 5: Has IP host
    ip_pattern = r"^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$"
    features[5] = 1.0 if re.match(ip_pattern, host.split(":")[0]) else 0.0
    
    # Feature 6: Domain entropy (normalized, max 5.0)
    features[6] = min(calculate_entropy(host) / 5.0, 1.0)
    
    # Feature 7: Path entropy (normalized, max 5.0)
    features[7] = min(calculate_entropy(path) / 5.0, 1.0)
    
    # Feature 8: Query param count (normalized, max 10)
    query_count = path_and_query.count("&") + (1 if "?" in path_and_query else 0)
    features[8] = min(query_count / 10.0, 1.0)
    
    # Feature 9: Has @ symbol
    features[9] = 1.0 if "@" in url else 0.0
    
    # Feature 10: Number of dots (normalized, max 10)
    features[10] = min(url.count(".") / 10.0, 1.0)
    
    # Feature 11: Number of dashes (normalized, max 10)
    features[11] = min(url.count("-") / 10.0, 1.0)
    
    # Feature 12: Has port number
    has_port = ":" in host and host.split(":")[-1].isdigit()
    features[12] = 1.0 if has_port else 0.0
    
    # Feature 13: Shortener domain
    shorteners = {"bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly"}
    features[13] = 1.0 if any(s in host for s in shorteners) else 0.0
    
    # Feature 14: Suspicious TLD
    suspicious_tlds = {"tk", "ml", "ga", "cf", "gq", "xyz", "icu", "top"}
    tld = host.split(".")[-1] if "." in host else ""
    features[14] = 1.0 if tld in suspicious_tlds else 0.0
    
    return features


def calculate_entropy(text: str) -> float:
    """Calculate Shannon entropy of a string."""
    if not text:
        return 0.0
    
    freq = {}
    for char in text:
        freq[char] = freq.get(char, 0) + 1
    
    length = len(text)
    entropy = 0.0
    for count in freq.values():
        p = count / length
        entropy -= p * log2(p)
    
    return entropy


def sigmoid(x: float) -> float:
    """Sigmoid activation function."""
    return 1.0 / (1.0 + np.exp(-x))


class LogisticRegressionModel:
    """Simple logistic regression for phishing detection."""
    
    def __init__(self, weights: np.ndarray = None, bias: float = 0.0):
        self.weights = weights if weights is not None else np.zeros(15)
        self.bias = bias
    
    def predict(self, features: np.ndarray) -> float:
        """Predict phishing probability."""
        z = np.dot(self.weights, features) + self.bias
        return sigmoid(z)
    
    def fit(self, X: np.ndarray, y: np.ndarray, epochs: int = 1000, lr: float = 0.1):
        """Train the model using gradient descent."""
        n_samples, n_features = X.shape
        self.weights = np.zeros(n_features)
        self.bias = 0.0
        
        for _ in range(epochs):
            # Forward pass
            z = np.dot(X, self.weights) + self.bias
            predictions = sigmoid(z)
            
            # Compute gradients
            dw = (1 / n_samples) * np.dot(X.T, (predictions - y))
            db = (1 / n_samples) * np.sum(predictions - y)
            
            # Update weights
            self.weights -= lr * dw
            self.bias -= lr * db
    
    def to_json(self) -> dict:
        """Export model to JSON format."""
        return {
            "weights": self.weights.tolist(),
            "bias": float(self.bias)
        }


def generate_synthetic_dataset(n_samples: int = 1000) -> Tuple[np.ndarray, np.ndarray]:
    """Generate synthetic training data."""
    X = []
    y = []
    
    # Safe URLs
    safe_patterns = [
        "https://www.google.com/search?q=test",
        "https://github.com/user/repo",
        "https://www.amazon.com/dp/B08ABC123",
        "https://stackoverflow.com/questions/123",
        "https://en.wikipedia.org/wiki/Python"
    ]
    
    # Phishing URLs
    phishing_patterns = [
        "http://192.168.1.1/login.php",
        "https://paypa1-secure.tk/verify",
        "http://amaz0n.ml/tracking",
        "https://secure.bank.com.evil.ga/login",
        "http://bit.ly/x3Yz123"
    ]
    
    for _ in range(n_samples // 2):
        # Safe sample (with small variations)
        url = np.random.choice(safe_patterns)
        X.append(extract_features(url))
        y.append(0)
        
        # Phishing sample (with small variations)
        url = np.random.choice(phishing_patterns)
        X.append(extract_features(url))
        y.append(1)
    
    return np.array(X), np.array(y)


def main():
    print("Mehr Guard ML Model Training")
    print("=" * 40)
    
    # Generate training data
    print("Generating synthetic dataset...")
    X_train, y_train = generate_synthetic_dataset(1000)
    
    # Train model
    print("Training logistic regression model...")
    model = LogisticRegressionModel()
    model.fit(X_train, y_train, epochs=2000, lr=0.5)
    
    # Evaluate
    predictions = np.array([model.predict(x) for x in X_train])
    binary_preds = (predictions > 0.5).astype(int)
    accuracy = np.mean(binary_preds == y_train)
    
    print(f"Training accuracy: {accuracy:.2%}")
    print(f"\nModel weights: {model.weights}")
    print(f"Model bias: {model.bias:.4f}")
    
    # Export to JSON
    model_json = model.to_json()
    with open("models/phishing_model_weights.json", "w") as f:
        json.dump(model_json, f, indent=2)
    
    print("\nModel exported to models/phishing_model_weights.json")
    
    # Test predictions
    print("\nTest predictions:")
    test_urls = [
        "https://www.google.com",
        "http://paypa1-secure.tk/login",
        "https://bit.ly/abc123"
    ]
    
    for url in test_urls:
        features = extract_features(url)
        prob = model.predict(features)
        verdict = "PHISHING" if prob > 0.5 else "SAFE"
        print(f"  {url[:40]:<40} -> {prob:.2%} ({verdict})")


if __name__ == "__main__":
    main()
