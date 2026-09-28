from collections import Counter
from app.schemas.ggaia_schemas import ComponentPool, DesignDraft, Mechanic, NewGame

def draft_feasibility(
    draft: DesignDraft,
    pool: ComponentPool,
    mechanics_by_id: dict[str, Mechanic]
) -> list[str]:
    issues: list[str] = []
    all_mechanics = draft.mechanics.core + draft.mechanics.supporting + draft.mechanics.structural

    for mech in all_mechanics:
        mechanic = mechanics_by_id.get(mech.mechanic_id)

        if mechanic is None:
            issues.append(f"Mechanic with id: {mech.mechanic_id} does not exist")
            continue

        if not pool.mechanic_feasibility(mechanic):
            missing = [mech_type 
                       for mech_type in mechanic.requires_component_types 
                       if pool.quantity_per_type(mech_type) == 0]
            issues.append(f"The mechanic '{mechanic.name}' requires components of type {missing} which are not present in the pool.")

    return issues

def validate_game_against_pool(
    game: NewGame,
    pool: ComponentPool
) -> list[str]:
    issues: list[str] = []
    for component in game.components:

        if not pool.component_count_feasible(component.component_id, component.quantity):
            bad_component = pool.get_component_by_id(component.component_id)
            
            if bad_component is None:
                issues.append(f"Game references component_id: {component.component_id} which does not exist in component pool.")
            else:
                issues.append(f"Game requires {component.quantity}x '{bad_component.name}' but the pool only has {bad_component.quantity}")

    return issues

def duplicate_component_overuse(game: NewGame, pool: ComponentPool) -> list[str]:
    totals: Counter = Counter()
    for entry in game.components:
        totals[entry.component_id] += entry.quantity

    issues: list[str] = []
    for comp_id, total in totals.items():
        comp = pool.get_component_by_id(comp_id)
        if comp is None:
            continue # checked in the validate_game_against_pool util 

        matches = [entry for entry in game.components if entry.component_id == comp_id]
        if len(matches) > 1 and total > comp.quantity and all(match.quantity <= comp.quantity for match in matches):
            issues.append(
                f"Component_id: {comp_id} is listed {len(matches)} times for a total of {total}x "
                f"'{comp.name}', but the pool only has {comp.quantity}. Merge the occurences and reduce quantity."
            )

    return issues