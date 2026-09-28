import { ref, watch ,computed, onScopeDispose, type Ref } from 'vue'
import { SetupWizardService, type SetupWizard } from '~/services/setupWizardService'

const POLL_MS = 5000

export const useSetupWizard = () =>{
    const wizard = ref<SetupWizard | null>(null)
    const error = ref<string | null>(null)
    let timer: ReturnType <typeof setTimeout> | null = null

    const status = computed(() => wizard.value?.job.status ?? null)
    const progress = computed(() => wizard.value?.job.progress ?? 0)
    const isLoading = computed(()=> status.value === 'queued' || status.value === 'running')

    const stop = () =>{
        if(timer) clearTimeout(timer)
            timer = null
    }

    const poll = async(wizardId: string) =>{
        try{
            wizard.value = await SetupWizardService.getWizard(wizardId);
        }catch(e: any){
            error.value = e?.message ?? 'Failed to fetch setup wizard';
            return;
        }

        const s = wizard.value.job.status
        if(s === 'ready') return;
        if(s === 'failed'){
            error.value = wizard.value.job.error ?? 'Setup wizard failed'
            return
        }
        timer = setTimeout(()=> poll(wizardId), POLL_MS)
    }

    const start = async(rulebookId: string) =>{
        stop();
        error.value = null;
        try{
            wizard.value = await SetupWizardService.createOrGetWizard(rulebookId);
        }
        catch(e: any){
            error.value = e?.message ?? 'Failed to start setup wizard'
            return;
        }

        if(wizard.value.job.status !== 'ready'){
            await poll(wizard.value.id)
        }
    }
    onScopeDispose(stop);

    return {wizard, status , progress, isLoading, error, start, stop}

}

export const toChecklistItems = (wizard: SetupWizard): ChecklistItem[] =>
    wizard.components.map(comp =>{
        const usedIn = wizard.phases.find(p=>
            p.steps.some(s =>s.component_refs.some(r => r.id === comp.id))
        )
        return {
            id: comp.id,
            title: comp.quantity != null ? `${comp.quantity} ${comp.name}`: comp.name,
            category: usedIn?.label?? 'Components',
            description: '',
            checked: false,
        }
    })

export const toWizardStepDefs = (wizard: SetupWizard):WizardStepDef[]=>
    wizard.phases.flatMap(phase => phase.steps.map(step =>({
        number: step.order,
        phase: phase.label,
        title: step.title,
        componentIds: step.component_refs.map(r => r.id),
        description: step.instruction 
    }))
)
export interface ChecklistPill { label: string; color?: string; border?: boolean }
export interface ChecklistItem {
    id: string
    title: string
    category: string
    description: string
    pills?: string[]
    customPills?: ChecklistPill[]
    checked: boolean
}

export interface WizardStepDef { number: number; phase: string, title: string, description: string, componentIds: string[] }
export interface ActiveSetup { id: string; title: string; coverImage?: string; step: number; totalSteps: number }


export const useSetupChecklist = (wizard: Ref<SetupWizard | null>) => {
    const steps = computed(() => (wizard.value ? toWizardStepDefs(wizard.value) : []))
    const checklist = ref<ChecklistItem[]>([])
    const stepNumber = ref(1)

    watch(
        () => wizard.value?.job.status,
        status => {
            if (status === 'ready' && wizard.value) {
                checklist.value = toChecklistItems(wizard.value)
                stepNumber.value = 1
            }
        },
        { immediate: true }
    )

    const totalSteps = computed(() => steps.value.length)
    const currentStep = computed(() => steps.value[stepNumber.value - 1])

    const stepChecklist = computed(() => {
        const ids = currentStep.value?.componentIds ?? []
        return checklist.value.filter(c => ids.includes(c.id))
    })

    const checkedCount = computed(() => stepChecklist.value.filter(c => c.checked).length)
    const allConfirmed = computed(() => stepChecklist.value.every(c => c.checked))

    const toggleItem = (id: string) => {
        const item = checklist.value.find(c => c.id === id)
        if (item) item.checked = !item.checked
    }

    const toggleAll = () => {
        const target = !allConfirmed.value
        stepChecklist.value.forEach(c => { c.checked = target })
    }

    const nextStep = () => {
        if (stepNumber.value < totalSteps.value) stepNumber.value++
    }

    return {
        stepChecklist,
        stepNumber,
        totalSteps,
        checkedCount,
        allConfirmed,
        currentStep,
        toggleItem,
        toggleAll,
        nextStep,
    }
}

const STORAGE_KEY = 'boardwise_active_setup'
const activeSetup = ref<ActiveSetup | null>(
    import.meta.client ? JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null') : null
)

export const useActiveSetup = () => {
    const setActiveSetup  = (setup: ActiveSetup) => {
        activeSetup.value = setup
        if (import.meta.client) localStorage.setItem(STORAGE_KEY, JSON.stringify(setup))
    }

    const clearActiveSetup = () => {
        activeSetup.value = null
        if (import.meta.client) localStorage.removeItem(STORAGE_KEY)
    }

    const restartSetup = () => {
        if (activeSetup.value) setActiveSetup({ ...activeSetup.value, step: 1 })
    }

    return { 
        activeSetup, 
        setActiveSetup,
        clearActiveSetup,
        restartSetup
    }
}