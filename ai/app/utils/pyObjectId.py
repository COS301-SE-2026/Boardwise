from bson import ObjectId
from pydantic import GetCoreSchemaHandler
from pydantic_core import core_schema 

class PyObjectId(str):
    @classmethod 
    def __get_pydantic_core_schema__(cls, source, handler: GetCoreSchemaHandler):
        """
        Validate Mongo based Objectid's
        """
        return core_schema.no_info_wrap_validator_function(
            cls._validate, core_schema.str_schema()
        )

    @classmethod
    def _validate(cls, v, handler):
        if isinstance(v, ObjectId):
            return str(v)
        if isinstance(v, str) and ObjectId.is_valid(v):
            return v
        raise ValueError("Invalid ObjectId")