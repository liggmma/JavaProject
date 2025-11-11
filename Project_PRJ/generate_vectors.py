from sentence_transfor mers import SentenceTransformer
import pandas as pd
import numpy as np
import json
import requests
from PIL import Image
import torchvision.transforms as transforms
import torchvision.models as models
import torch
from io import BytesIO
import faiss

# --- Load text embedding model ---
text_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')

# --- Load ResNet50 model for image embedding ---
img_model = models.resnet50(pretrained=True)
img_model = torch.nn.Sequential(*list(img_model.children())[:-1])
img_model.eval()

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225])
])

def extract_image_vector(url):
    response = requests.get(url)
    response.raise_for_status()
    image = Image.open(BytesIO(response.content)).convert("RGB")
    tensor = transform(image).unsqueeze(0)
    with torch.no_grad():
        vec = img_model(tensor).squeeze().numpy()
    return vec / np.linalg.norm(vec)

# --- Load data ---
product_df = pd.read_csv("products.csv", sep="|")
image_df = pd.read_csv("product_images.csv", sep="|")

# --- Generate text embeddings ---
text_inputs = (product_df["name"] + " " + product_df["description"]).tolist()
text_embeddings = text_model.encode(text_inputs, convert_to_numpy=True)

# --- Save text embeddings ---
with open("text_vectors.json", "w", encoding="utf-8") as f:
    json.dump({
        "products": product_df.to_dict(orient="records"),
        "vectors": text_embeddings.tolist()
    }, f)

# --- Process image embeddings ---
image_vectors = []
image_metadata = []

for _, row in image_df.iterrows():
    pid = row["product_id"]
    url = row["image_url"]
    try:
        vec = extract_image_vector(url)
        image_vectors.append(vec.astype(np.float32))
        prod_info = product_df[product_df["product_id"] == pid].iloc[0].to_dict()
        image_metadata.append({
            **prod_info,
            "image_url": url
        })
        print(f"✅ Processed: {url}")
    except Exception as e:
        print(f"❌ Error with {url}: {e}")

# --- Save image FAISS index and metadata ---
if image_vectors:
    image_np = np.array(image_vectors, dtype=np.float32)
    index = faiss.IndexFlatL2(image_np.shape[1])
    index.add(image_np)
    faiss.write_index(index, "image_index.faiss")

    with open("image_metadata.json", "w", encoding="utf-8") as f:
        json.dump(image_metadata, f, ensure_ascii=False, indent=2)

print("✅ Hoàn tất xử lý embedding văn bản và hình ảnh.")
