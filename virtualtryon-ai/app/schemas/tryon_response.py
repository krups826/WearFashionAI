from pydantic import BaseModel


class TryOnResponse(BaseModel):
    request_id: str
    status: str
    message: str
    output_image: str | None = None