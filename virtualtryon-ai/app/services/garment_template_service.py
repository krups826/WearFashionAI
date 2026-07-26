import os


GARMENT_TEMPLATE_DIR = "templates/garments"


class GarmentTemplateService:

    GARMENT_TEMPLATES = {
        "FORMAL_SHIRT": "formalshirt.webp"
    }

    def get_template(
        self,
        garment_type: str
    ) -> str:

        normalized_type = (
            garment_type
            .strip()
            .upper()
        )

        template_name = self.GARMENT_TEMPLATES.get(
            normalized_type
        )

        if template_name is None:
            raise ValueError(
                f"Unsupported garment type: {garment_type}"
            )

        template_path = os.path.join(
            GARMENT_TEMPLATE_DIR,
            template_name
        )

        if not os.path.isfile(template_path):
            raise FileNotFoundError(
                f"Garment template not found: {template_path}"
            )

        return template_path