import logging 

from app.schemas.setup_wizard_schemas import LLMStep
from app.retrieval.setup_wizard_retrieval import get_setup_chunks
from app.generation.setup_wizard.setup_wizard_generation import generate_phase
from app.generation.setup_wizard.setup_wizard_prompt import PHASE_ORDER
from app.generation.setup_wizard.setup_wizard_assembly import assemble_wizard_output
from app.services import mongo_service

logger = logging.getLogger(__name__)

def run_setup_wizard_job(wizard_id: str, rulebook_id: str, ml_models: dict) -> None:
    """
    BackgroundTasks entry point: retrieval -> per-phase generation -> assembly + grounding -> persist.
    Never raises,every failure path writes to job.error so the API only ever needs to poll Mongo.
    """

    try: 
        mongo_service.update_setup_wizard_job(wizard_id, "running", progress= 0)

        chunks = get_setup_chunks(rulebook_id, ml_models, 12)
        if not chunks:
            raise ValueError("No setup-relevant chunks were retrieved for this rulebook.")
        chunks_by_index = {c["index"]: c["content"] for c in chunks}
        steps_by_phase: dict[str, list[LLMStep]] = {}
        total_phases = len(PHASE_ORDER)

        for i, phase in enumerate(PHASE_ORDER, start= 1):
            steps_by_phase[phase] = generate_phase(phase,chunks,ml_models)
            progress = round((i/total_phases)* 90)
            mongo_service.update_setup_wizard_job(wizard_id, "running", progress=progress)

        if not any(steps_by_phase.values()):
            raise ValueError("Generation produced no steps in any phase.")

        output = assemble_wizard_output(steps_by_phase,chunks_by_index)
        mongo_service.finalise_setup_wizard(wizard_id,output)

        logger.info(
            "Setup wizard %s completed: %d steps across %d phases.",
            wizard_id, output["summary"]["total_steps"], len(output["phases"]),
        )

    except Exception as exc: 
        logger.exception("Setup wizard job %s failed.", wizard_id)
        try:
            mongo_service.update_setup_wizard_job(wizard_id, "failed", error=str(exc))
        except Exception:
            logger.exception("Also failed to write failure status for wizard %s.", wizard_id)