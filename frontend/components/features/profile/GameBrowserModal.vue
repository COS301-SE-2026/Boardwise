<template>
    <BaseModal v-model="open" :max-width="760">
        <div class="modal">
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

            <div
                v-if="isSearching"
                class="d-flex justify-center pa-6"
            >
                <v-progress-circular
                    indeterminate
                    color="primary"
                />
            </div>

            <div v-if="selectedGames.length" class="selected-bar mb-3">
                <v-icon size="16" color="primary">mdi-check-circle</v-icon>
                {{ selectedGames.length }} game{{ selectedGames.length > 1 ? 's' : '' }} selected
            </div>

            <div class="gamesGrid mb-4">
                <div
                    v-for="game in filteredGames"
                    :key="game.id"
                    class="gameCard card"
                    :class="{ 'gameCard_selected': isSelected(game) ,
                        'gameCard_owned' :isOwned(game)
                    }"
                    @click="handleGameClick(game)"
                >

                <div class="gameCard_image">
                    <div 
                        v-if="isOwned(game)"
                        class="gameCard_overlay" gameCard_ownedOverlay
                    >
                        <v-icon size="28">mdi-check-circle</v-icon>
                    </div>

                    <div v-if="isSelected(game)" class="gameCard_overlay float-right">
                        <v-icon color="primary" size="28">mdi-check-circle</v-icon>
                    </div>

                    <v-img
                        :width="131"
                        aspect-ratio="16/9"
                        cover
                        :src="game.imageUrl ?? '/default.png'"
                    ></v-img>
                </div>

                <div class="gameCard_content">
                    <p class="gameCard_title">{{ game.title }}</p>
                    <p class="gameCard_genre">{{ game.genre?.[0] ?? '' }}</p>

                    <!-- Duplicate warning -->
                    <p v-if="isOwned(game)" class="duplicate-warning"><v-icon size="14">mdi-alert-circle</v-icon>Already in your collection</p>
                </div>
            </div>

            <!-- No results -->
            <BaseEmptyState 
                v-if="!filteredGames.length && search.trim() && !isSearching"
                title="No games found"
                description="Try searching for another board game."
            />

            <!-- Empty search -->
            <BaseEmptyState
                v-if="!search.trim() && !isSearching"
                title="Search for a game"
                description="Search our game library to add a board game to your collection."
            />


                <div class="d-flex justify-space-between align-center">
                    <BaseButton variant="secondary" @click="$emit('add-custom')">
                        + Add unlisted game
                    </BaseButton>

                    <BaseButton :disabled="!selectedGames.length" @click="handleConfirm">
                        <v-progress-circular
                            v-if="isSubmitting"
                            indeterminate
                            size="16"
                            width="2"
                            class="mr-2"
                        />
                        Add {{ selectedGames.length > 0 ? selectedGames.length : '' }} 
                        Game{{ selectedGames.length !== 1 ? 's' : '' }}
                    </BaseButton>
                </div>
            </div>
        </div>
    </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { ref, computed, watch } from 'vue'
import { userService } from '~/services/userService'
import { useDebounceFn } from '@vueuse/core'

const props = defineProps({
    games: { 
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['confirm', 'add-custom'])

const open = defineModel()
const search = ref('')
const selectedGames = ref([])
const games = ref([]);
const isSearching = ref(false);
const isSubmitting = ref(false)

const handleSearch = async(query) => {
     if (!query || !query.trim()) {
        games.value = []
        return
    }

    isSearching.value = true

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

const isOwned = (game) => {
    return props.games.some(ownedGame => ownedGame.id === game.id)
}

const isSelected = (game) => selectedGames.value.some(g => g.id === game.id)

const handleGameClick = (game) => {
    if(isOwned(game)) {
        return
    }

    toggleGame(game)
}

const toggleGame = (game) => {
    if (isSelected(game)) {
        selectedGames.value = selectedGames.value.filter(g => g.id !== game.id)
    } else {
        selectedGames.value.push(game)
    }
}

const filteredGames = computed(() => games.value)

const handleConfirm = async () => {
    if(!selectedGames.value.length) {
        return
    }

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

<style scoped>
.gamesGrid {
    display: grid;
    grid-template-columns:
        repeat(auto-fill, minmax(150px, 1fr));
    gap: var(--space-4);
}

.gameCard {
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: .2s ease;

  background: white;
}

.gameCard:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.gameCard_selected {
  border-color: var(--color-primary);
}

.gameCard_owned {
    cursor: not-allowed;
    opacity: .75;
}

.gameCard_owned:hover {
    transform: none;
    box-shadow: none;
}


.gameCard_image {
    position: relative;
}

.gameCard_overlay {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 2;
}

.gameCard_ownedOverlay {
    color: var(--color-text-muted);
}

.gameCard_content {
    padding: var(--space-3);
}

.gameCard_title {
    margin: 0;
    font-weight: var(--fw-bold);
    line-height: var(--lh-tight);
}

.gameCard_genre {
    margin: var(--space-1) 0 0;
    color: var(--color-text-muted);
    font-size: var(--fs-small);
}

.duplicate-warning {
    display: flex;
    align-items: center;
    gap: var(--space-1);
    margin: var(--space-2) 0 0;
    font-size: var(--fs-small);
    color: var(--color-text-muted);
}

.selected-bar {
    display: flex;
    align-items: center;
    gap: var(--space-2);
}

.modal-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--space-3);
}

@media (max-width: 600px) {
    .gamesGrid {
        grid-template-columns:
            repeat(2, minmax(0, 1fr));
    }

    .modal-actions {
        flex-direction: column;
        align-items: stretch;
    }
}
</style>