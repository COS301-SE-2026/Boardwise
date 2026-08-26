import logging

logger = logging.getLogger(__name__)


def build_chat_messages(query: str, retrieved_chunks: list[dict]) -> list[dict]:
    """
    Constructs the message payload for the LLM chat completion API.
    Enforces a strict adjudicator persona using XML boundaries to prevent
    hallucinations in smaller fallback models.
    """

    context_texts = []
    for i, chunk in enumerate(retrieved_chunks, start=1):
        context_texts.append(f"[Excerpt{i}]\n{chunk.get('content', '')}")

    context_block = "\n\n".join(context_texts)

    system_prompt = (
        "You are an expert tabletop board game rules adjudicator. "
        "Your sole purpose is to answer user question using ONLY the text provided inside the <context> tags. "
        "Adhere strictly to these rules:\n"
        "1. Do not use outside knowledge, assume rules, or hallucinate mechanics.\n"
        "2. If the <context> does not explicitly contain the answer, you must reply exactly with: "
        "'I cannot find the answer to this rule in the provided text.'\n"
        "3. Be concise, direct, and clear in your explanation."
    )

    user_prompt = (
        f"<context>\n"
        f"{context_block}\n"
        f"</context>\n\n"
        f"<question>\n"
        f"{query}\n"
        f"</question>\n\n"
        f"Answer the <question> based strictly on the <context> above."
    )

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

    return messages
