<template>
    <BaseCard class="auth-card" data-test="auth-card">
        <div class="form">
            <h2 class="form-title" data-test="auth-title">{{ title }}</h2>

            <BaseInput 
                v-for="field in fields"
                :key="field.key"
                v-model="values[field.key]"
                :type="field.type ?? 'text'"
                :placeholder="field.placeholder"
                :data-test="`input-${field.key}`"
            />

            <BaseButton 
                block
                size="large"
                class="mt-2"
                data-test="submit-button"
                @click="submitForm"
            >
                {{ buttonText }}
            </BaseButton>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    title: String,
    buttonText: String,
    fields: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['submit'])

const values = reactive(
    Object.fromEntries(props.fields.map(f => [f.key, '']))
)

const submitForm = () => {
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
</style>
