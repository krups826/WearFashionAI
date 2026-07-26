import os

import numpy as np
from PIL import Image


GARMENT_OUTPUT_DIR = "input/garment"

os.makedirs(
    GARMENT_OUTPUT_DIR,
    exist_ok=True
)


class FabricMappingService:

    def generate_garment(
        self,
        fabric_path: str,
        template_path: str,
        request_id: str
    ) -> str:

        print("LOADING GARMENT TEMPLATE AND FABRIC...")

        template = Image.open(
            template_path
        ).convert("RGB")

        fabric = Image.open(
            fabric_path
        ).convert("RGB")

        template_array = np.array(
            template
        ).astype(np.float32)

        fabric_array = np.array(
            fabric
        ).astype(np.float32)

        average_fabric_color = (
            fabric_array
            .reshape(-1, 3)
            .mean(axis=0)
        )

        print(
            "AVERAGE FABRIC COLOR:",
            average_fabric_color
        )

        gray = (
            template_array[:, :, 0] * 0.299
            + template_array[:, :, 1] * 0.587
            + template_array[:, :, 2] * 0.114
        )

        brightness = (
            gray / 255.0
        )[:, :, None]

        generated_array = (
            average_fabric_color
            * brightness
        )

        generated_array = np.clip(
            generated_array,
            0,
            255
        ).astype(np.uint8)

        generated_garment = Image.fromarray(
            generated_array
        )

        output_path = os.path.join(
            GARMENT_OUTPUT_DIR,
            f"{request_id}_garment.png"
        )

        generated_garment.save(
            output_path
        )

        print(
            "GENERATED GARMENT:",
            output_path
        )

        return output_path