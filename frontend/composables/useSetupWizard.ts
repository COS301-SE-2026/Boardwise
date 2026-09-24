import { ref, computed } from 'vue'

export interface ChecklistPill { label: string; color?: string; border?: boolean }
export interface ChecklistItem {
    id: number
    title: string
    category: string
    description: string
    pills?: string[]
    customPills?: ChecklistPill[]
    checked: boolean
}

export interface WizardStepDef { number: number; phase: string, title: string, description: string }
export interface ActiveSetup { id: string; title: string; coverImage?: string; step: number; totalSteps: number }

// TODO: no backend for wizard 

const MOCK_STEPS: WizardStepDef[] = [
    { number: 1, phase: 'Phase 1: Inventory & Preparation', title: 'Step 1: Check Your Components', description: 'Verify starting pieces before setting up the board.' },
    { number: 2, phase: 'Phase 1: Inventory & Preparation', title: 'Step 2: Tabletop Alignment & Placement', description: 'Follow spatial guidelines to lay hexes and frames seamlessly.' },
    { number: 3, phase: 'Phase 2: Player Setup' ,title: 'Step 3: Distribute Player Sets', description: 'Hand out roads, settlements and cities to each player.' },
    { number: 4, phase: 'Phase 2: Player Setup' ,title: 'Step 4: Prepare the Bank', description: 'Sort resource and development cards into their stacks.' },
    { number: 5, phase: 'Phase 3: Final Checks' ,title: 'Step 5: Final Checks', description: 'Confirm the robber, dice and tokens are placed and ready.' }
]

const MOCK_CHECKLIST: Omit<ChecklistItem, 'checked'>[] = [
    {
        id: 1,
        title: '19 Hexagonal Terrain Tiles',
        category: 'Island Base',
        description: 'Ensure all terrain types are accounted for before shuffling.',
        pills: ['Forest x4', 'Hill x3', 'Pasture x4', 'Fields x4', 'Mountains x3', 'Desert x1']
    },
    {
        id: 2,
        title: '6 Frame Pieces',
        category: 'Perimeter',
        description: 'Outer sea borders numbered 1 through 6 with 9 coastal harbors.',
        pills: ['Tabs 1-6 intact', '9 Coastal Harbors (5 generic, 4 special)']
    },
    {
        id: 3,
        title: '95 Resource & 25 Development Cards',
        category: 'Bank Stacks',
        description: 'Stored into 5 distinct resource pills + face-down dev card deck.',
        pills: ['19 of each Resource', '14 Knights - 5 VPs - 6 Progress']
    },
    {
        id: 4,
        title: '4 Player Sets (24 Wooden Pieces Each)',
        category: 'Player Stock',
        description: 'Each set includes 15 Roads, 5 Settlements, and 4 Cities.',
        customPills: [
            { label: 'Red (P1)', color: '#E53935' },
            { label: 'Blue (P2)', color: '#3949AB' },
            { label: 'White (P3)', color: '#FFFFFF', border: true },
            { label: 'Orange (P4)', color: '#FB8C00' }
        ]
    },
    {
        id: 5,
        title: '18 Number Tokens, Dice & Robber',
        category: 'Production',
        description: 'Letters A through R on reverse. Confirm red 6 and 8 tokens are present.',
        pills: ['Tokens A-R', '2 Six-sided dice', '1 Robber pawn', 'Longest Road & Largest Army']
    }
]

export const useSetupChecklist = () => {
    const checklist = ref<ChecklistItem[]>(MOCK_CHECKLIST.map(item => ({ ...item, checked: false })))
    const stepNumber = ref(1)
    const totalSteps = MOCK_STEPS.length

    const checkedCount = computed(() => checklist.value.filter(c => c.checked).length)
    const allConfirmed = computed(() => checkedCount.value === checklist.value.length)
    const currentStep = computed(() => MOCK_STEPS[stepNumber.value -1])

    const toggleItem = (id: number) => {
        const item = checklist.value.find(c => c.id === id)
        if (item) item.checked = !item.checked
    }

    const toggleAll = () => {
        const target = !allConfirmed.value
        checklist.value.forEach(c => { c.checked = target })
    }

    const nextStep = () => {
        if (stepNumber.value < totalSteps) stepNumber.value++
    }

    return { 
        checklist, 
        stepNumber, 
        totalSteps, 
        checkedCount, 
        allConfirmed, 
        currentStep, 
        toggleItem, 
        toggleAll, 
        nextStep
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