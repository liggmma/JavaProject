from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
import numpy as np
import json

app = Flask(__name__)

# --- Load model và dữ liệu ---
model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')

with open("text_vectors.json", "r", encoding="utf-8") as f:
    data = json.load(f)
    product_vectors = np.array(data["vectors"], dtype=np.float32)
    product_list = data["products"]

# --- Hàm tìm kiếm sản phẩm tương đồng nhất ---
def find_similar_products(query, top_k=8):
    query_vec = model.encode(query, convert_to_numpy=True)
    query_vec = query_vec / np.linalg.norm(query_vec)

    similarities = np.dot(product_vectors, query_vec)
    top_indices = similarities.argsort()[-top_k:][::-1]

    results = [product_list[i] for i in top_indices]
    return results

# --- Route tìm kiếm ---
@app.route("/similar-products", methods=["POST"])
def search():
    data = request.get_json()
    summary = data.get("summary", "")

    if not summary:
        return jsonify({"error": "Missing 'summary' in request"}), 400

    results = find_similar_products(summary)
    return jsonify({"results": results})

# --- Chạy server ---
if __name__ == "__main__":
    app.run(debug=True)

