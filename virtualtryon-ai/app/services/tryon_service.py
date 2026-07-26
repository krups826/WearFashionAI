import os
import shutil
import uuid

from fastapi import UploadFile

from app.services.garment_template_service import (
    GarmentTemplateService
)

from app.services.fabric_mapping_service import (
    FabricMappingService
)

from app.services.gpu_tryon_service import (
    GpuTryOnService
)


PERSON_DIR = "input/person"
FABRIC_DIR = "input/fabric"
GARMENT_DIR = "input/garment"
OUTPUT_DIR = "output"


os.makedirs(
    PERSON_DIR,
    exist_ok=True
)

os.makedirs(
    FABRIC_DIR,
    exist_ok=True
)

os.makedirs(
    GARMENT_DIR,
    exist_ok=True
)

os.makedirs(
    OUTPUT_DIR,
    exist_ok=True
)


class TryOnService:

    def __init__(self):

        self.garment_template_service = (
            GarmentTemplateService()
        )

        self.fabric_mapping_service = (
            FabricMappingService()
        )

        self.gpu_tryon_service = (
            GpuTryOnService()
        )


    def generate(
        self,
        person: UploadFile,
        fabric: UploadFile,
        garment_type: str
    ):

        # --------------------------------
        # CREATE REQUEST ID
        # --------------------------------

        request_id = str(
            uuid.uuid4()
        )


        # --------------------------------
        # GET GARMENT TEMPLATE
        # --------------------------------

        garment_template_path = (
            self.garment_template_service
            .get_template(
                garment_type
            )
        )


        # --------------------------------
        # GET FILE EXTENSIONS
        # --------------------------------

        person_extension = (
            self._get_extension(
                person.filename
            )
        )

        fabric_extension = (
            self._get_extension(
                fabric.filename
            )
        )


        # --------------------------------
        # CREATE INPUT PATHS
        # --------------------------------

        person_path = os.path.join(
            PERSON_DIR,
            f"{request_id}_person{person_extension}"
        )

        fabric_path = os.path.join(
            FABRIC_DIR,
            f"{request_id}_fabric{fabric_extension}"
        )


        # --------------------------------
        # SAVE PERSON IMAGE
        # --------------------------------

        self._save_file(
            person,
            person_path
        )


        # --------------------------------
        # SAVE FABRIC IMAGE
        # --------------------------------

        self._save_file(
            fabric,
            fabric_path
        )


        # --------------------------------
        # GENERATE FABRIC-BASED GARMENT
        # --------------------------------

        generated_garment_path = (
            self.fabric_mapping_service
            .generate_garment(
                fabric_path=fabric_path,
                template_path=garment_template_path,
                request_id=request_id
            )
        )


        # --------------------------------
        # CALL COLAB GPU API
        # --------------------------------

        print(
            "================================"
        )

        print(
            "SENDING GENERATED GARMENT TO GPU"
        )

        print(
            "================================"
        )


        final_output_path = (
            self.gpu_tryon_service
            .generate_tryon(
                person_path=person_path,
                garment_path=generated_garment_path,
                request_id=request_id
            )
        )


        # --------------------------------
        # PRINT FINAL DETAILS
        # --------------------------------

        print(
            "================================"
        )

        print(
            "TRY-ON GENERATION COMPLETE"
        )

        print(
            "================================"
        )

        print(
            "REQUEST ID:",
            request_id
        )

        print(
            "PERSON:",
            person_path
        )

        print(
            "FABRIC:",
            fabric_path
        )

        print(
            "GARMENT TYPE:",
            garment_type
        )

        print(
            "GARMENT TEMPLATE:",
            garment_template_path
        )

        print(
            "GENERATED GARMENT:",
            generated_garment_path
        )

        print(
            "FINAL OUTPUT:",
            final_output_path
        )


        # --------------------------------
        # FINAL RESPONSE
        # --------------------------------

        return {
            "request_id": request_id,
            "status": "COMPLETED",
            "message": (
                "Virtual try-on generated successfully"
            ),
            "output_image": final_output_path
        }


    # --------------------------------
    # SAVE UPLOADED FILE
    # --------------------------------

    def _save_file(
        self,
        upload_file: UploadFile,
        destination: str
    ):

        with open(
            destination,
            "wb"
        ) as file:

            shutil.copyfileobj(
                upload_file.file,
                file
            )


    # --------------------------------
    # GET FILE EXTENSION
    # --------------------------------

    def _get_extension(
        self,
        filename: str | None
    ):

        if filename is None:
            return ".jpg"

        extension = os.path.splitext(
            filename
        )[1]

        if not extension:
            return ".jpg"

        return extension