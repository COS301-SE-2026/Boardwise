from unittest.mock import ANY, patch

from app.dependencies import verify_jwt
from app.main import app
from bson import ObjectId


class TestUploadRulebook:
    """Groups all integration tests for POST /api/vault/rulebooks/upload"""

    def test_upload_rejects_non_pdf(self, client, mock_auth):
        """Proves the Gateway rejects invalid file types instantly."""
        # Arrange
        files = {"file": ("malware.sh", b"echo 'hacked'", "application/x-sh")}
        data = {"title": "Hack", "language": "en"}

        # Act
        response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

        # Assert
        assert response.status_code == 415
        assert "Only PDF files" in response.json()["detail"]

    @patch("app.routers.rulebook.mongo_service")
    @patch("app.routers.rulebook.BackgroundTasks.add_task")
    def test_upload_success_mocked(
        self, mock_add_task, mock_mongo, client, mock_auth, minimal_pdf
    ):
        """Proves that a valid upload returns a 202 and starts the background job."""
        # Setup mocks
        mock_mongo.create_rulebook_and_job.return_value = (
            "mock_rulebook_123",
            "mock_job_123",
        )

        # Arrange
        files = {"file": ("catan.pdf", minimal_pdf, "application/pdf")}
        data = {"title": "Catan", "language": "en"}

        # Act
        response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

        # Assert
        assert response.status_code == 202
        assert response.json()["rulebookId"] == "mock_rulebook_123"

        mock_mongo.create_rulebook_and_job.assert_called_once()
        mock_add_task.assert_called_once()

    # ========== Edge Case Testing ==========
    @patch("app.routers.rulebook.settings.MAX_FILE_SIZE_MB", 0)
    def test_upload_rejects_pdf_too_large(self, client, mock_auth):
        """Proves that upload rejects files exceeding the size limit"""
        # Arrange
        tiny_pdf = b"%PDF-1.4\n"
        files = {"file": ("test.pdf", tiny_pdf, "application/pdf")}
        data = {"title": "Dune", "language": "en"}

        # Act
        response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

        # Assert
        assert response.status_code == 413
        assert "File exceeds 0MB limit" in response.json()["detail"]

    def test_upload_rejects_non_pdf_bytes(self, client, mock_auth):
        """Proves that upload rejects files containing non-pdf bytes"""
        # Arrange
        non_pdf = b"%NotPDF-1.4\n"
        files = {"file": ("test.pdf", non_pdf, "application/pdf")}
        data = {"title": "Dune", "language": "en"}

        # Act
        response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

        # Assert
        assert response.status_code == 422
        assert "The uploaded file is empty or corrupted." in response.json()["detail"]

    def test_upload_rejects_token_missing_sub_claim(self, client):
        """Proves that upload rejects files uploaded by contributors with a missing sub claim in their token"""

        # Arrange
        def override_verify_jwt_no_sub():
            return {
                "jti": "valid_token_id_123",
                "iat": 1700000000,
            }

        app.dependency_overrides[verify_jwt] = override_verify_jwt_no_sub

        try:
            dummy_file = {"file": ("test.pdf", b"%PDF-1.4\n", "application/pdf")}
            data = {"title": "Dune", "language": "en"}

            # Act
            response = client.post(
                "/api/vault/rulebooks/upload",
                data=data,
                files=dummy_file,
                headers={"Authorization": "Bearer fake_token"},
            )

            # Assert
            assert response.status_code == 401
            assert response.json()["detail"] == "sub is missing from token."
        finally:
            app.dependency_overrides.clear()

    @patch("app.routers.rulebook.mongo_service")
    def test_upload_throws_value_error(
        self, mock_mongo, client, mock_auth, minimal_pdf
    ):
        """Proves that upload fails if a value related errror occurs"""
        # Arrange
        mock_mongo.create_rulebook_and_job.side_effect = ValueError(
            "Boardgame 'Catan' not found."
        )

        # Arrange
        files = {"file": ("catan.pdf", minimal_pdf, "application/pdf")}
        data = {"title": "Catan", "language": "en"}

        # Act
        response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

        # Assert
        assert response.status_code == 400
        assert response.json()["detail"] == "Upload rejected"

        mock_mongo.create_rulebook_and_job.assert_called_once()

    @patch("app.routers.rulebook.mongo_service")
    def test_upload_throws_error_for_unexpected_failure(
        self, mock_mongo, client, mock_auth, minimal_pdf
    ):
        """Proves that upload fails if an internal server errror occurs"""
        # Arrange
        mock_mongo.create_rulebook_and_job.side_effect = Exception(
            "Unexpected error occurred."
        )

        # Arrange
        files = {"file": ("catan.pdf", minimal_pdf, "application/pdf")}
        data = {"title": "Catan", "language": "en"}

        # Act
        response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

        # Assert
        assert response.status_code == 500
        assert (
            response.json()["detail"]
            == "An internal server error occurred while initialising the upload."
        )


@patch("app.routers.rulebook.generate_answer")
@patch("app.routers.rulebook.build_chat_messages")
@patch("app.routers.rulebook.retrieve_context")
class TestQueryRulebook:
    """Groups all integration tests for POST /api/vault/rulebooks/{rulebookId}/query"""

    def test_query_rulebook_valid_request_returns_200_with_answer(
        self,
        mock_retrieve_context,
        mock_build_chat_messages,
        mock_generate_answer,
        client,
        mock_auth,
        mock_verify_index_ready,
    ):

        # Arrange
        rulebook_id = str(ObjectId())
        query_text = "How many cards do I draw?"
        payload = {"query": query_text}

        mock_chunks = [{"chunkId": "1", "content": "Draw two cards."}]
        mock_retrieve_context.return_value = mock_chunks

        mock_messages = [{"role": "user", "content": "..."}]
        mock_build_chat_messages.return_value = mock_messages

        expected_answer = "You draw two cards per turn."
        mock_generate_answer.return_value = expected_answer

        # Act
        response = client.post(
            f"/api/vault/rulebooks/{rulebook_id}/query", json=payload
        )

        # Assert
        assert response.status_code == 200

        response_data = response.json()
        assert response_data["answer"] == expected_answer
        assert response_data["citations"][0]["chunkId"] == "1"

        mock_retrieve_context.assert_called_once_with(query_text, rulebook_id, ANY)
        mock_build_chat_messages.assert_called_once_with(query_text, mock_chunks)
        mock_generate_answer.assert_called_once_with(mock_messages, ANY)
