<template>
    <BaseModal v-model="open" :max-width="760">

        <div class="d-flex align-center justify-space-between mb-4">
            <h2>Add games to your collection</h2>
            <v-btn icon variant="text" @click="open = false">
                <v-icon>mdi-close</v-icon>
            </v-btn>
        </div>

        <BaseSearch 
            v-model="search"
            placeholder="Search our game library..."
            class="mb-4"
        />

        <div v-if="selectedGames.length" class="selected-bar mb-3">
            <v-icon size="16" color="primary">mdi-check-circle</v-icon>
            {{ selectedGames.length }} game
            {{ selectedGames.length > 1 ? 's' : '' }} selected
        </div>

        <div class="gamesGrid mb-4">
            <div
                v-for="game in filteredGames"
                :key="game.id"
                class="gameCard card"
                :class="{ 'gameCard_selected': isSelected(game) }"
                @click="toggleGame(game)"
            >

            <div class="gameCard_image">
                <img :src="game.imageUrl ?? '/default-game.png'" :alt="game.title" />

                <div v-if="isSelected(game)" class="gameCard_overlay">
                    <v-icon color="white" size="28">mdi-check-circle</v-icon>
                </div>
        </div>

        <p class="gameCard_title">{{ game.title }}</p>
        <p class="gameCard_genre">{{ game.genre?.[0] ?? '' }}</p>
            </div>

            <div class="d-flex justify-space-between align-center">
                <BaseButton variant="secondary" @click="$emit('add-custom')">
                    + Add unlisted game
                </BaseButton>

                <BaseButton :disabled="!selectedGames.length" @click="handleConfirm">
                    Add {{ selectedGames.length > 0 ? selectedGames.length : '' }} 
                    Game {{ selectedGames.length !== 1 ? 's' : '' }}
                </BaseButton>
            </div>
        </div>
    </BaseModal>
</template>

<script setup>
import { ref, computed } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const open = defineModel()
const emit = defineEmits(['confirm', 'add-custom'])

const search = ref('')
const selectedGames = ref([])

const mockGames = [
  { id: 1, title: 'Monopoly',       imageUrl: null, genre: ['Family'] },
  { id: 2, title: 'Scrabble',       imageUrl: null, genre: ['Family'] },
  { id: 3, title: 'Catan',          imageUrl: null, genre: ['Strategy'] },
  { id: 4, title: 'Ticket to Ride', imageUrl: null, genre: ['Strategy'] },
  { id: 5, title: 'Dixit',          imageUrl: null, genre: ['Party'] },
  { id: 6, title: 'Azul',           imageUrl: null, genre: ['Abstract'] },
  { id: 7, title: 'Pandemic',       imageUrl: null, genre: ['Cooperative'] },
  { id: 8, title: 'Codenames',      imageUrl: null, genre: ['Party'] },
]

const filteredGames = computed(() => {
    if(!search.value) return mockGames
    const q = search.value.toLowerCase()
    return mockGames.filter(g => g.title.toLowerCase().includes(q))
})

const isSelected = (game) => selectedGames.value.some(g => g.id === game.id)

const toggleGame = (game) => {
    if (isSelected(game)) {
        selectedGames.value = selectedGames.value.filter(g => g.id !== game.id)
    } else {
        selectedGames.value.push(game)
    }
}

const handleConfirm = () => {
  emit('confirm', selectedGames.value)
  selectedGames.value = []
  open.value = false
}
</script>