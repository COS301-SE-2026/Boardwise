<template>
    <div class="overflow-hidden py-6">
        <div class="game-strip ga-3 marquee-left">
            <NuxtLink
                v-for="(game, index) in topRow"
                :key="`top-${game.id}-${index}`"
                :to="`/library`"
                class="text-decoration-none"
            >
                <BaseCard class="game-card pa-0" > 
                    <BaseImage
                        :src="game.image"
                        :alt="game.title"
                        height="160" 
                        cover
                    />
                </BaseCard>
            </NuxtLink>
        </div>

        <div class="game-strip ga-3 marquee-left">
            <NuxtLink
                v-for="(game, index) in middleRow"
                :key="`middle-${game.id}-${index}`"
                :to="`/library/${game.id}`"
                class="text-decoration-none"
            >
                <BaseCard class="game-card pa-0" > 
                    <BaseImage
                        :src="game.image"
                        :alt="game.title"
                        height="160" 
                        cover
                    />
                </BaseCard>
            </NuxtLink>
        </div>

        <div class="game-strip ga-3 marquee-right">
            <NuxtLink
                v-for="(game, index) in bottomRow"
                :key="`bottom-${game.id}-${index}`"
                :to="`/library`"
                class="text-decoration-none"
            >
                <BaseCard class="game-card pa-0"  > 
                    <BaseImage
                        :src="game.image"
                        height="160px" 
                        cover
                    />
                </BaseCard>
            </NuxtLink>
        </div>

        
    </div>
</template>

<script setup>
import { computed} from 'vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import { getLandingGames } from '~/services/landingService'

const games = getLandingGames()

const repeatedGames = computed(() =>{
    return new Array(5).fill(games).flat()
})

const topRow = computed(() =>
    repeatedGames.value.filter((_, index) => index % 3 === 0)
)
const middleRow = computed(() =>
    repeatedGames.value.filter((_, index) => index % 3 === 1)
)
const bottomRow = computed(() =>
    repeatedGames.value.filter((_, index) => index % 3 === 2)
)
</script>