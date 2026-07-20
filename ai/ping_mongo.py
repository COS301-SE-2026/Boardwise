import os
from dotenv import load_dotenv
from pymongo import MongoClient
from pymongo.errors import ConnectionFailure

load_dotenv("../.env")

client = MongoClient(os.getenv("DB_URL"))

try:
    client.admin.command('ping')
    print("Database connection successful")

except ConnectionFailure:
    print("Server not available")