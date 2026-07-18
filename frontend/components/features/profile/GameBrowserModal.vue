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
                <!-- <img :src="game.imageUrl ?? '/default-game.png'" :alt="game.title" /> -->

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
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { ref, computed, watch } from 'vue'
import { userService } from '~/services/userService'
import { useDebounceFn } from '@vueuse/core'


const open = defineModel()
const emit = defineEmits(['confirm', 'add-custom'])

const search = ref('')
const selectedGames = ref([])
const games = ref([]);
const isSearching = ref(false);

const handleSearch = async(query)=>{
     if (!query || !query.trim()) {
        games.value = [];
        return;
    }

    isSearching.value = true;
    try{
        const res = await userService.searchForBoardGame(query);
        console.log(res);
        games.value = res.boardGames;
    }
    catch(err){
        console.error("search failed: ", err);
        games.value = [];
    }
    finally{
        isSearching.value = false;
    }
}

const debouncedSearch = useDebounceFn(handleSearch, 300)

watch(search, (val) => {
    debouncedSearch(val)
}, { immediate: true });// empty query = null


const filteredGames = computed(() => games.value)

const isSelected = (game) => selectedGames.value.some(g => g.id === game.id)

const toggleGame = (game) => {
    if (isSelected(game)) {
        selectedGames.value = selectedGames.value.filter(g => g.id !== game.id)
    } else {
        selectedGames.value.push(game)
    }
}

const isSubmitting = ref(false)

const handleConfirm = async () => {
    isSubmitting.value = true
    try {
        const results = await Promise.allSettled(
            selectedGames.value.map(game => userService.addExistingGameToInventory(game.id))
        )

        const failed = results.filter(r => r.status === 'rejected')
        if (failed.length) {
            console.error(`${failed.length} game(s) failed to add`, failed)
        }

        emit('confirm', selectedGames.value)
        selectedGames.value = []
        open.value = false
    }
    finally {
        isSubmitting.value = false
    }
}
</script>