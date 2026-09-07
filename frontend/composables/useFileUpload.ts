import { ref } from 'vue'

export function useFileUpload() {
    const fileInput = ref<HTMLInputElement | null>(null)
    const file = ref<File | null>(null)
    const fileName = ref('')

    const trigger = () => fileInput.value?.click()

    const handleChange = (e: Event) => {
        const target = e.target as HTMLInputElement
        const chosen = target.files?.[0]
        if (chosen) {
            file.value = chosen
            fileName.value = chosen.name
        }
    }

    const reset = () => {
        file.value = null
        fileName.value = ''
        if (fileInput.value) fileInput.value.value = ''
    }

    return { fileInput, file, fileName, trigger, handleChange, reset }
}