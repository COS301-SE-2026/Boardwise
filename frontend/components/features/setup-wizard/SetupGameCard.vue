<template>
    <BaseCard clickable class="setup-game-card" @click="$emit('launch', game)">
        <template #media>
            <BaseImage :src="coverImage" :alt="game.title" height="176px" fit="cover" />
            <BaseBadge variant="obsidian" absolute>{{  badgeLabel }}</BaseBadge>
        </template>

        <div class="setup-game-card__heading">
            <h3 class="card-title">{{  game.title  }}</h3>
            <span class="setup-game-card__players">{{  playersLabel  }}</span>
        </div>

        <p class="card-meta setup-game-card__desc">
            {{  game.description || 'Step-by-step setup guidance grounded in the official rulebook.' }}
        </p>

        <template #actions>
            <div class="setup-game-card__footer">
                <span class="setup=game-card__verified">
                    <v-icon size="16" color="var(--color-success)">mdi-check-decagram</v-icon>
                    Official Rulebook
                </span>

                <BaseButton size="small" @click.stop="$emit('launch',game)">Launch Wizard</BaseButton>
            </div>
        </template>
    </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import BaseImage from '~/components/ui/BaseImage.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'


const props = defineProps<{ game : any}>()
defineEmits<{ (e: 'launch', game: any) : void}>()

const coverImage = computed(() => props.game.coverImage || props.game.BaseImage || '/images/BoarleySide.svg')
const badgeLabel = computed(() => (Array.isArray(props.game.genre)) ? props.game.genre[0] : props.game.genre || 'Base Game')
const playersLabel = computed (() => {
    if (props.game.minPlayers && props.game.maxPlayers) return `${props.game.minPlayers}-&{props.game.maxPlayers} Players`
    return props.game.players || ''
})
</script>