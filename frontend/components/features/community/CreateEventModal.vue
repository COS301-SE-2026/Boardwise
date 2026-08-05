<template>
    <BaseModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
  >

  <div class=" d-flex flex-column ga-6 ">
    <h2>
        Create Event
    </h2>

    <BaseInput
        v-model="form.name"
        label="Event nme"
    />

    <BaseInput
        v-model="form.game"
        label="Game"
    />

    <BaseInput
        v-model="form.location"
        label=" location"
    />

    <div class="d-flex ga-4" >
        <BaseInput
            v-model="form.date"
            type="date"
            label="Date"
            class="flex-1"
        />

        <BaseInput
            v-model="form.time"
            type="time"
            label="Time"
            class="flex-1"
        />
    </div>

    <BaseTextArea  
        v-model="form.description" 
        placeholder="Event description"
        :rows="4"
        />

    <div class="d-flex justify-end ga-3">
        <BaseButton
            variant="secodary"
            @click="$emit('update:modelValue',false)"
        >
        Cancel
    </BaseButton>

    <BaseButton @click="createEvent" >
        Create Event
    </BaseButton>
    
    </div>
</div>
</BaseModal>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue';
import BaseInput from '~/components/ui/BaseInput.vue';
import BaseTextArea from '~/components/ui/BaseTextArea.vue';
import BaseModal from '~/components/ui/BaseModal.vue';
import { reactive } from 'vue'

defineProps({
    modelValue: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits([
    'update:modelValue',
    'create'
])

const form = reactive({
    name:'',
    game: '',
    location:'',
    date:'',
    time:'',
    description:''
})

const createEvent = () => {
    emit('create', { ...form })

    emit('update:modelValue', false)

    Object.keys(form).forEach(key => {
        form[key] = ' '
        })
}
</script>
