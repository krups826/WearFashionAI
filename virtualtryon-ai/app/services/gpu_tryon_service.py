import os

import requests


OUTPUT_DIR = "output"

os.makedirs(
    OUTPUT_DIR,
    exist_ok=True
)


class GpuTryOnService:

    def __init__(self):

        # IMPORTANT:
        # Paste ONLY the base ngrok URL.
        # Do not add /health.
        # Do not add /api/v1/gpu/tryon here.

        self.gpu_api_base_url = os.getenv(
            "GPU_API_BASE_URL",
            "https://mutt-udder-easter.ngrok-free.dev"
        )

        self.gpu_tryon_url = (
            f"{self.gpu_api_base_url}/tryon"
        )


    def generate_tryon(
        self,
        person_path: str,
        garment_path: str,
        request_id: str
    ) -> str:

        output_path = os.path.join(
            OUTPUT_DIR,
            f"{request_id}_tryon.png"
        )

        print("================================")
        print("CALLING COLAB GPU API")
        print("================================")

        print(
            "GPU URL:",
            self.gpu_tryon_url
        )

        print(
            "PERSON:",
            person_path
        )

        print(
            "GARMENT:",
            garment_path
        )


        with open(
            person_path,
            "rb"
        ) as person_file, open(
            garment_path,
            "rb"
        ) as garment_file:

            files = {
                "person": (
                    os.path.basename(person_path),
                    person_file,
                    "image/jpeg"
                ),
                "garment": (
                    os.path.basename(garment_path),
                    garment_file,
                    "image/png"
                )
            }

            response = requests.post(
                self.gpu_tryon_url,
                files=files,
                timeout=600
            )


        print(
            "GPU RESPONSE STATUS:",
            response.status_code
        )


        if response.status_code != 200:

            print(
                "GPU ERROR RESPONSE:",
                response.text
            )

            raise RuntimeError(
                "GPU try-on failed. "
                f"Status: {response.status_code}. "
                f"Response: {response.text}"
            )


        content_type = response.headers.get(
            "content-type",
            ""
        )

        print(
            "GPU CONTENT TYPE:",
            content_type
        )


        if "image/" not in content_type.lower():

            raise RuntimeError(
                "GPU API did not return an image. "
                f"Content-Type: {content_type}. "
                f"Response: {response.text}"
            )


        with open(
            output_path,
            "wb"
        ) as output_file:

            output_file.write(
                response.content
            )


        print(
            "FINAL TRY-ON SAVED:",
            output_path
        )

        return output_path