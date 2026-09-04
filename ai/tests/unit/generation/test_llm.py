from unittest.mock import MagicMock, patch

from app.generation.llm import generate_answer


@patch("app.generation.llm.hf_client")
def test_generate_answer_successful_api_call_returns_stripped_text(mock_hf_client):
    # Arrange
    messages = [{"role": "user", "content": "How do I win?"}]
    ml_models = {}
    expected_clean_answer = "You must score 10 victory points."

    mock_message = MagicMock()
    mock_message.content = f"     {expected_clean_answer}    "

    mock_choice = MagicMock()
    mock_choice.message = mock_message

    mock_response = MagicMock()
    mock_response.choices = [mock_choice]

    mock_hf_client.chat_completion.return_value = mock_response

    # Act
    answer = generate_answer(messages, ml_models)

    # Assert
    assert answer == expected_clean_answer

    mock_hf_client.chat_completion.assert_called_once_with(
        messages=messages, max_tokens=500, temperature=0.1, stream=False
    )
