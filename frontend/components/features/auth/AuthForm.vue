<template>
    <BaseCard class="auth-card" data-test="auth-card">
        <v-form ref="formRef" class="form" @submit.prevent="submitForm">
            <h2 class="form-title" data-test="auth-title">{{ title }}</h2>
            <p v-if="subtitle" class="form-subtitle" data-test="auth-subtitle">{{ subtitle }}</p>

            <BaseInput 
                v-for="field in fields"
                :key="field.key"
                v-model="values[field.key]"
                :type="field.type ?? 'text'"
                :label="field.label"
                :rules="getFieldRules(field)"
                :placeholder="field.placeholder"
                :data-test="`input-${field.key}`"
            />

            <slot name="after-fields" />

            <BaseButton 
                block
                size="large"
                class="mt-2"
                data-test="submit-button"
                @click="submitForm"
            >
                {{ buttonText }}
            </BaseButton>
        </v-form>
    </BaseCard>
</template>

<script setup>
import { ref, reactive } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    title: String,
    subtitle: String,
    buttonText: String,
    fields: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['submit'])

const formRef = ref(null)

const values = reactive(
    Object.fromEntries(props.fields.map(f => [f.key, '']))
)

const getFieldRules = (field) => {
    const baseRules = field.rules || []
    if(field.key === 'confirmPassword') {
        return [
            ...baseRules,
            (v) => v === values.password || 'Passwords do not match'
        ]
    }

    return baseRules
}

const submitForm = async () => {
    const { valid } = await formRef.value.validate()
    if (!valid) return 
    
    emit('submit', { ...values })
}

</script>

<style scoped>
.auth-card {
    width: 100%;
    max-width: 520px;
    margin: auto;
    padding: 2.5rem;
}

.form {
    display: flex;
    flex-direction: column;
    gap: 16px;
} 

.form-title {
    text-align: center;
    font-family: var(--font-display);
    font-size: var(--fs-h2);
    color: var(--color-secondary);
    margin-bottom: .5rem;
}

.form-subtitle {
    text-align: center;
    font-size: var(--fs-body-sm, .875rem);
    color: var(--color-text-muted);
    margin-top: -.5rem;
    margin-bottom: .5rem;
}

.forgot-link {
    text-align: right;
    margin-top: -8px;
    margin-bottom: -4px;
}
</style>
