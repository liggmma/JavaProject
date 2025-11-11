from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
import faiss
import json
import numpy as np
from PIL import Image
import torch
import torchvision.models as models
import torchvision.transforms as transforms
import io
import requests

app = Flask(__name__)

# ----- Load văn bản model & vector -----
print("🔄 Loading text model and product vectors...")
text_model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")

with open("text_vectors.json", "r", encoding="utf-8") as f:
    text_data = json.load(f)

products = text_data["products"]
text_vectors = np.array(text_data["vectors"]).astype("float32")

text_index = faiss.IndexFlatL2(text_vectors.shape[1])
text_index.add(text_vectors)

# ----- Load image model & vector -----
print("🔄 Loading image model and product image vectors...")
image_model = models.resnet50(pretrained=True)
image_model = torch.nn.Sequential(*list(image_model.children())[:-1])
image_model.eval()

with open("image_metadata.json", "r", encoding="utf-8") as f:
    image_metadata = json.load(f)

image_index = faiss.read_index("image_index.faiss")

# ----- Helper xử lý ảnh -----
def extract_vector_from_bytes(img_bytes):
    image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    transform = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225])
    ])
    tensor = transform(image).unsqueeze(0)
    with torch.no_grad():
        vec = image_model(tensor).squeeze().numpy()
    return vec / np.linalg.norm(vec)

def extract_vector_from_url(url):
    response = requests.get(url)
    response.raise_for_status()
    return extract_vector_from_bytes(response.content)

# ----- Xử lý tìm kiếm văn bản -----
@app.route("/search", methods=["POST"])
def search_text():
    query = request.json.get("query", "")
    if not query:
        return jsonify({"error": "Missing query"}), 400

    vec = text_model.encode([query]).astype("float32")
    distances, indices = text_index.search(vec, len(products))

    results = []
    for j, i in enumerate(indices[0]):
        product = products[i]
        if "product_id" in product:
            similarity = float(1.0 - distances[0][j])
            results.append({
                "product_id": product["product_id"],
                "score": similarity
            })

    return jsonify(results)

# ----- Xử lý tìm kiếm hình ảnh -----
@app.route("/search-image", methods=["POST"])
def search_image():
    if 'image' in request.files:
        file = request.files["image"]
        vec = extract_vector_from_bytes(file.read())
    elif request.json and 'image_url' in request.json:
        vec = extract_vector_from_url(request.json['image_url'])
    else:
        return jsonify({"error": "Missing image or image_url"}), 400

    vec = vec.astype(np.float32).reshape(1, -1)
    D, I = image_index.search(vec, len(image_metadata))

    score_by_product = {}
    for j, i in enumerate(I[0]):
        meta = image_metadata[i]
        product_id = meta.get("product_id") or meta.get("id")
        score = float(D[0][j])
        if product_id:
            if product_id not in score_by_product or score < score_by_product[product_id]:
                score_by_product[product_id] = score

    results = [
        {"product_id": pid, "score": s}
        for pid, s in score_by_product.items()
    ]

    results.sort(key=lambda x: x["score"])
    return jsonify(results)

if __name__ == "__main__":
    app.run(port=5000)
