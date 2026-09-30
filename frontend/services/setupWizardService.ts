export type JobStatus = "queued" | "running" | "ready" | "failed";
export type PhaseKey = "board" | "components" | "players" | "first_player"; 
export type StepScope = "shared" | "per_player";
export type StepConfidence = "verified" | "flagged";

export interface SetupWizard{
    id: string;
    rulebookId: string;
    createdAt: string;
    updatedAt: string;
    schemaVersion: number;
    job: WizardJob;
    game: string | null
    config: WizardConfig;
    summary: WizardSummary | null;
    components: WizardComponent[];
    phases: WizardPhase[];
    warnings: string[]
}

export interface WizardJob{
    status: JobStatus;
    progress: number;
    generatedAt: string | null;
    error: string | null; 
}

export interface WizardConfig{
    minPlayers: number;
    maxPlayers: number;
}

export interface WizardSummary{
    total_steps: number;
    estimated_minutes: number;
    verified_steps: number;
}

export interface WizardComponent{
    id: string
    name: string
    quantity: string |number | null;
}

export interface WizardPhase{
    id: PhaseKey;
    label: string;
    steps: WizardStep[]
}

export interface WizardStep{
    id: string;
    order: number;
    title: string;
    instruction: string;
    component_refs: ComponentRef[];
    scope: StepScope;
    confidence: StepConfidence;
    sources: StepSource[];
}

export interface ComponentRef{
    id: string;
    quantity: string |number | null;
}

export interface StepSource{
    chunk_index: number;
    excerpt: string;
}

export const SetupWizardService = {
    createOrGetWizard(rulebook_id :string){
        const {$fastApi} = useNuxtApp()
        return $fastApi<SetupWizard>(`/vault/rulebooks/${rulebook_id}/setup-wizard`, {
            method: 'POST'
        });
    },

    getWizard(wizardId: string){
        const { $fastApi } = useNuxtApp();
        return $fastApi<SetupWizard>(`/vault/rulebooks/setup-wizard/${wizardId}`);
    }

}
