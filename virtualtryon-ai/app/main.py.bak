import os

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import FileResponse

from app.services.tryon_service import TryOnService


app = FastAPI(
    title="WearFashion Virtual Try-On AI",
    description="Fabric-based AI Virtual Try-On Service",
    version="1.0.0"
)


tryon_service = TryOnService()


@app.get("/")
def root():

    return {
        "status": "UP",
        "service": "WearFashion Virtual Try-On AI"
    }


@app.get("/health")
def health():

    return {
        "status": "UP",
        "service": "virtualtryon-ai"
    }


@app.post("/api/v1/tryon/generate")
def generate_tryon(
    person: UploadFile = File(...),
    fabric: UploadFile = File(...),
    garment_type: str = Form(...)
):

    print("================================")
    print("TRYON API HIT")
    print("================================")

    return tryon_service.generate(
        person=person,
        fabric=fabric,
        garment_type=garment_type
    )


@app.get("/api/v1/tryon/image/{request_id}")
def get_tryon_image(
    request_id: str
):

    output_path = os.path.join(
        "output",
        f"{request_id}_tryon.png"
    )

    print("================================")
    print("TRY-ON IMAGE REQUEST")
    print("================================")

    print(
        "REQUEST ID:",
        request_id
    )

    print(
        "OUTPUT PATH:",
        output_path
    )


    if not os.path.isfile(
        output_path
    ):

        raise HTTPException(
            status_code=404,
            detail="Try-on image not found"
        )


    return FileResponse(
        path=output_path,
        media_type="image/png",
        filename=f"{request_id}_tryon.png"
    )