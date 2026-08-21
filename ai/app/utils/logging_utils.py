def sanitise_log_input(text: str) -> str:
    """
    Sanitises user input for logging by stripping newline and carriage return characters
    to prevent Log Injection / Log Forging attacks.
    """
    if not isinstance(text, str):
        text = str(text)
    return text.replace("\n", "_").replace("\r", "_")
