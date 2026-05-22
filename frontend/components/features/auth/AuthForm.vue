<template>
    <BaseCard>
        <div class="form">
            <h2 class="text-center mb-6">{{ title }}</h2>

            <BaseInput 
                v-for="field in fields"
                :key="field.key"
                v-model="values[field.key]"
                :type="field.type ?? 'text'"
                :placeholder="field.placeholder"
            />

            <BaseButton @click="submitForm">
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
.form {
    display: flex;
    flex-direction: column;
    gap: 16px;
} 
</style>
