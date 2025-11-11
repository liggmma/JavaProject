import json
import numpy as np
from sentence_transformers import SentenceTransformer

# --- Load model ---
model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")

# --- Câu hỏi từ người dùng ---
query = "Tôi cần áo sơ mi công sở lịch sự"

# --- Mã hóa query thành vector ---
query_vec = model.encode(query)

# --- Load dữ liệu sản phẩm + vector từ file JSON ---
with open("text_vectors.json", "r", encoding="utf-8") as f:
    data = json.load(f)

products = data["products"]
vectors = np.array(data["vectors"])  # (số sản phẩm, 384)

# --- Tính cosine similarity ---
def cosine_similarity(a, b):
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))

# --- Tính similarity cho từng sản phẩm ---
similarities = [cosine_similarity(query_vec, vec) for vec in vectors]

# --- Lấy top sản phẩm gần nhất ---
top_index = int(np.argmax(similarities))
top_product = products[top_index]

# --- Hiển thị kết quả ---
print(" Gợi ý sản phẩm gần nhất với câu hỏi:")
print(f"Câu hỏi: {query}")
print(f"Sản phẩm phù hợp nhất:")
print(f"- Tên: {top_product['name']}")
print(f"- Mô tả: {top_product['description']}")
print(f"- Độ tương đồng: {similarities[top_index]:.4f}")
