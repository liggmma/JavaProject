from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np
import json

app = Flask(__name__)

# Load SentenceTransformer model
model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')

# Load vectors from JSON
with open('text_vectors.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

products = [item["product"] for item in data]
vectors = np.array([item["vector"] for item in data])

@app.route("/search", methods=["POST"])
def search_similar():
    try:
        # Lấy chuỗi mô tả từ client Java
        user_input = request.json.get("query", "")
        if not user_input:
            return jsonify({"error": "No query provided"}), 400

        # Tạo embedding vector cho chuỗi đầu vào
        input_vector = model.encode(user_input)

        # Tính độ tương đồng cosine
        scores = cosine_similarity([input_vector], vectors)[0]

        # Lấy top 6 sản phẩm có vector tương đồng nhất
        top_indices = scores.argsort()[-6:][::-1]
        results = [{"product": products[i], "similarity": float(scores[i])} for i in top_indices]

        return jsonify({"results": results}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5000)
