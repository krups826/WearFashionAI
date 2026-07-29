import os
import requests

print(">>> GPU_TRYON_SERVICE LOADED <<<")

OUTPUT_DIR = "output"
os.makedirs(OUTPUT_DIR, exist_ok=True)


class GpuTryOnService:

    def __init__(self):

        self.gpu_api_base_url = os.getenv(
            "GPU_API_BASE_URL",
            "https://YOUR-NGROK-URL.ngrok-free.app"   # <-- Replace with your current ngrok URL
        )

        self.gpu_tryon_url = (
            self.gpu_api_base_url + "/tryon"
        )

    def generate_tryon(
        self,
        person_path: str,
        garment_path: str,
        request_id: str
    ) -> str:

        print("================================")
        print("GPU TRYON SERVICE")
        print("================================")

        print("PERSON :", person_path)
        print("GARMENT :", garment_path)

        output_path = os.path.join(
            OUTPUT_DIR,
            f"{request_id}_tryon.png"
        )

        with open(person_path, "rb") as person_file, \
             open(garment_path, "rb") as garment_file:

            files = {
                "person": (
                    os.path.basename(person_path),
                    person_file,
                    "image/png"
                ),
                "garment": (
                    os.path.basename(garment_path),
                    garment_file,
                    "image/png"
                )
            }

            form_data = {
                "category": "shirt",
                "garment_photo_type": "flat-lay"
            }

            print("--------------------------------")
            print("POST URL :", self.gpu_tryon_url)
            print("FORM DATA :", form_data)
            print("FILES :", list(files.keys()))
            print("--------------------------------")

            response = requests.post(
                url=self.gpu_tryon_url,
                files=files,
                data=form_data,
                timeout=600
            )

        print("--------------------------------")
        print("STATUS :", response.status_code)
        print("--------------------------------")

        if response.status_code != 200:

            try:
                print(response.json())
            except Exception:
                print(response.text)

            raise Exception(
                f"GPU API Error : {response.status_code}"
            )

        with open(output_path, "wb") as f:
            f.write(response.content)

        print("TRYON SAVED :", output_path)

        return output_path