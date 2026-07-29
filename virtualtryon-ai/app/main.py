import os
import uuid
import shutil

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import FileResponse

from app.services.tryon_service import TryOnService

app = FastAPI(
    title="WearFashion Virtual Try-On AI",
    description="Fabric Based Virtual Try-On",
    version="1.0.0"
)

tryon_service = TryOnService()


# =====================================================
# ROOT
# =====================================================

@app.get("/")
def root():
    return {
        "status": "UP",
        "service": "WearFashion Virtual Try-On AI"
    }


@app.get("/health")
def health():
    return {
        "status": "UP"
    }


# =====================================================
# GENERATE GARMENT
# =====================================================

@app.post("/generate-garment")
def generate_garment(
    fabric: UploadFile = File(...),
    garment_type: str = Form(...)
):

    print("================================")
    print("GENERATE GARMENT API HIT")
    print("================================")

    request_id = str(uuid.uuid4())

    fabric_ext = os.path.splitext(fabric.filename)[1] or ".png"

    fabric_path = os.path.join(
        "uploads",
        "fabric",
        f"{request_id}_fabric{fabric_ext}"
    )

    os.makedirs(
        os.path.dirname(fabric_path),
        exist_ok=True
    )

    with open(fabric_path, "wb") as buffer:
        shutil.copyfileobj(
            fabric.file,
            buffer
        )

    normalized_type = garment_type.strip().upper()

    if normalized_type not in (
            tryon_service
            .garment_template_service
            .GARMENT_TEMPLATES
    ):
        normalized_type = "FORMAL_SHIRT"

    garment_template = (
        tryon_service
        .garment_template_service
        .get_template(normalized_type)
    )

    generated_garment = (
        tryon_service
        .fabric_mapping_service
        .generate_garment(
            fabric_path=fabric_path,
            template_path=garment_template,
            request_id=request_id
        )
    )

    destination = os.path.join(
        "outputs",
        "garment",
        f"{request_id}_garment.png"
    )

    os.makedirs(
        os.path.dirname(destination),
        exist_ok=True
    )

    shutil.copy(
        generated_garment,
        destination
    )

    return {
        "status": "SUCCESS",
        "requestId": request_id,
        "imageUrl": f"/outputs/garment/{request_id}_garment.png"
    }


# =====================================================
# GENERATE TRYON
# =====================================================

@app.post("/generate-tryon")
def generate_tryon(
    person: UploadFile = File(...),
    garment: UploadFile = File(...)
):

    print("================================")
    print("GENERATE TRYON API HIT")
    print("================================")

    request_id = str(uuid.uuid4())

    person_ext = os.path.splitext(person.filename)[1] or ".jpg"

    person_path = os.path.join(
        "uploads",
        "person",
        f"{request_id}_person{person_ext}"
    )

    os.makedirs(
        os.path.dirname(person_path),
        exist_ok=True
    )

    with open(person_path, "wb") as buffer:
        shutil.copyfileobj(
            person.file,
            buffer
        )

    garment_ext = os.path.splitext(garment.filename)[1] or ".png"

    garment_path = os.path.join(
        "uploads",
        "garment",
        f"{request_id}_garment{garment_ext}"
    )

    os.makedirs(
        os.path.dirname(garment_path),
        exist_ok=True
    )

    with open(garment_path, "wb") as buffer:
        shutil.copyfileobj(
            garment.file,
            buffer
        )

    print("PERSON :", person_path)
    print("GARMENT :", garment_path)

    final_output = (
        tryon_service
        .gpu_tryon_service
        .generate_tryon(
            person_path=person_path,
            garment_path=garment_path,
            request_id=request_id
        )
    )

    destination = os.path.join(
        "outputs",
        "tryon",
        f"{request_id}_tryon.png"
    )

    os.makedirs(
        os.path.dirname(destination),
        exist_ok=True
    )

    shutil.copy(
        final_output,
        destination
    )

    return {
        "status": "SUCCESS",
        "requestId": request_id,
        "imageUrl": f"/outputs/tryon/{request_id}_tryon.png"
    }


# =====================================================
# IMAGE APIs
# =====================================================

@app.get("/outputs/garment/{filename}")
def get_garment(filename: str):

    file_path = os.path.join(
        "outputs",
        "garment",
        filename
    )

    if not os.path.isfile(file_path):
        raise HTTPException(
            status_code=404,
            detail="Garment not found"
        )

    return FileResponse(file_path)


@app.get("/outputs/tryon/{filename}")
def get_tryon(filename: str):

    file_path = os.path.join(
        "outputs",
        "tryon",
        filename
    )

    if not os.path.isfile(file_path):
        raise HTTPException(
            status_code=404,
            detail="Try-On image not found"
        )

    return FileResponse(file_path)