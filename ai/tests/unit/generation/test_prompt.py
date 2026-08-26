from app.generation.prompt import build_chat_messages


def test_build_chat_messages_valid_chunks_returns_strict_adjudicator_payload():
    # Arrange
    query = "How do I move?"
    retrieved_chunks = [
        {"content": "Players move by rolling two six-sided dice."},
        {"content": "You cannot move through blocked terrain."},
    ]

    # Act
    messages = build_chat_messages(query, retrieved_chunks)

    # Assert
    assert len(messages) == 2

    assert messages[0]["role"] == "system"
    assert "expert tabletop board game rules adjudicator" in messages[0]["content"]
    assert "hallucinate mechanics" in messages[0]["content"]

    assert messages[1]["role"] == "user"
    assert "-- Rulebook Excerpt 1 --" in messages[1]["content"]
    assert retrieved_chunks[0]["content"] in messages[1]["content"]
    assert "-- Rulebook Excerpt 2 --" in messages[1]["content"]
    assert retrieved_chunks[1]["content"] in messages[1]["content"]

    assert f"User Question: {query}" in messages[1]["content"]
