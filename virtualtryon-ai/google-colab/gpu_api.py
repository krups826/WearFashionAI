import os
import uuid
import traceback

import torch
from PIL import Image

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import FileResponse, JSONResponse

from fashn_vton import TryOnPipeline

# --------------------------------------------------
# FastAPI
# --------------------------------------------------

app = FastAPI(
    title="WearFashion GPU API",
    version="1.0.0"
)

# --------------------------------------------------
# Directories
# --------------------------------------------------

WEIGHTS_DIR = "/content/fashn_weights"

INPUT_DIR = "/content/input"
OUTPUT_DIR = "/content/output"

PERSON_DIR = os.path.join(INPUT_DIR, "person")
GARMENT_DIR = os.path.join(INPUT_DIR, "garment")

os.makedirs(PERSON_DIR, exist_ok=True)
os.makedirs(GARMENT_DIR, exist_ok=True)
os.makedirs(OUTPUT_DIR, exist_ok=True)

# --------------------------------------------------
# Load Pipeline
# --------------------------------------------------

print("================================")
print("Loading FASHN-VTON Pipeline...")
print("================================")

pipeline = TryOnPipeline(
    weights_dir=WEIGHTS_DIR,
    device="cuda"
)

print("================================")
print("Pipeline Loaded Successfully")
print("================================")


# --------------------------------------------------
# Root
# --------------------------------------------------

@app.get("/")
def root():
    return {
        "service": "WearFashion GPU API",
        "status": "running"
    }


@app.get("/health")
def health():
    return {
        "status": "healthy",
        "cuda": torch.cuda.is_available(),
        "device": "cuda" if torch.cuda.is_available() else "cpu"
    }


# --------------------------------------------------
# Try-On
# --------------------------------------------------

@app.post("/tryon")
async def generate_tryon(
    person: UploadFile = File(...),
    garment: UploadFile = File(...),
    category: str = Form("tops"),
    garment_photo_type: str = Form("flat-lay")
):

    try:

        print("================================")
        print("TRYON REQUEST RECEIVED")
        print("================================")

        print("Person:", person.filename)
        print("Garment:", garment.filename)
        print("Category:", category)
        print("Garment Photo Type:", garment_photo_type)

        category = category.strip().lower()

        if category in [
            "shirt",
            "t-shirt",
            "tshirt",
            "top",
            "hoodie",
            "jacket",
            "sweater",
            "blazer",
            "tops"
        ]:
            category = "tops"

        elif category in [
            "pant",
            "pants",
            "jeans",
            "trouser",
            "bottoms"
        ]:
            category = "bottoms"

        elif category in [
            "dress",
            "gown",
            "one-pieces"
        ]:
            category = "one-pieces"

        else:
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported category: {category}"
            )

        print("Mapped Category:", category)

        person_path = os.path.join(
            PERSON_DIR,
            f"{uuid.uuid4()}.png"
        )

        garment_path = os.path.join(
            GARMENT_DIR,
            f"{uuid.uuid4()}.png"
        )

        with open(person_path, "wb") as f:
            f.write(await person.read())

        with open(garment_path, "wb") as f:
            f.write(await garment.read())

        person_image = Image.open(person_path).convert("RGB")
        garment_image = Image.open(garment_path).convert("RGB")

        print("Person Size:", person_image.size)
        print("Garment Size:", garment_image.size)

        result = pipeline(
            person_image=person_image,
            garment_image=garment_image,
            category=category,
            garment_photo_type=garment_photo_type,
            num_samples=1,
            num_timesteps=30,
            guidance_scale=1.5,
            seed=42,
            segmentation_free=True
        )

        output_image = result.images[0]

        output_path = os.path.join(
            OUTPUT_DIR,
            f"{uuid.uuid4()}.png"
        )

        output_image.save(output_path)

        print("================================")
        print("TRYON COMPLETED")
        print(output_path)
        print("================================")

        return FileResponse(
            output_path,
            media_type="image/png",
            filename="result.png"
        )

    except HTTPException:
        raise

    except Exception as e:

        traceback.print_exc()

        return JSONResponse(
            status_code=500,
            content={
                "success": False,
                "message": str(e),
                "trace": traceback.format_exc()
            }
        )