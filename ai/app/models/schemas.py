from pydantic import BaseModel
from bson import ObjectId

class Chunk(BaseModel):
    chunk_id: ObjectId
    index: int
    content: str