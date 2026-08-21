import logging

logger = logging.getLogger(__name__)

def build_chat_messages(query: str, retrieved_chunks: list[dict]) -> list[dict]:
    """
    Constructs the message payload for the LLM chat completion API.
    Enforces a strict adjudicator persona and provides the retrieved chunks as context.
    """
    
    context_texts = []
    for i, chunk in enumerate(retrieved_chunks, start=1):
        context_texts.append(f"--- Rulebook Excerpt {i} ---\n{chunk.get('content', '')}")
        
    context_block = "\n\n".join(context_texts)
    
    system_prompt = (
        "You are an expert tabletop board game rules adjudicator. "
        "Your sole purpose is to answer user questions about game mechanics using ONLY the provided rulebook excerpts. "
        "Adhere to these strict rules:\n"
        "1. Do not use outside knowledge, assume rules, or hallucinate mechanics.\n"
        "2. If the provided excerpts do not contain the answer, you must explicitly state: "
        "'I cannot find the answer to this rule in the provided text.'\n"
        "3. Be concise, direct, and clear in your explanation."
    )
    
    user_prompt = (
        f"Here are the relevant rulebook excerpts:\n\n"
        f"{context_block}\n\n"
        f"User Question: {query}\n\n"
        f"Answer the question based strictly on the excerpts above."
    )
    
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]
    
    return messages