<template>
    <BaseModal v-model="open">
        <div class="d-flex flex-column ga-6">

            <h2> Add game</h2>

            <BaseInput
                v-model="name"
                label="Game Name"
                placeholder="Enter game name"
            />

            <BaseInput
                v-model="category"
                :items="categories"
                label="Category"
                variant="outlined"
                rounded="lg"
            />
          
        <div class="d-flex justify-end ga-3">
            <BaseButton
                variant="secodary"
                @click="closeModal"
            >
                Cancel
            </BaseButton>

             <BaseButton
                @click="closeModal"
            >
                Add Game
            </BaseButton>
        </div>
        </div>
    
    </BaseModal>

</template>

<script setup>
import { ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseModal from '~/components/ui/BaseModal.vue'

const open = defineModel({
    type: Boolean,
    defualt: false
})

const emit = defineEmits(['confirm'])

const name = ref('')
const publisher = ref('')
const category = ref('Strategy')

const categories = [
    'Strategy',
    'Family',
    'Party',
    'Cooperative',
    'Card',
    'Abstract'
]

const closeModal = () => {
    open.value = false
    name.value = ''
    publisher.value = ''
    category.value = 'Strategy'
}

const handleAdd = () => {
    if(!name.value.trim()) return

    emit('confirm', {
        name: name.value.trim(),
        publisher: publisher.value.trim(),
        category: category.value
    })
}
</script>