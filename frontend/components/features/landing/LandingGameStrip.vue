<template>
    <section 
        class="landing-game-showcase" 
        aria-label="Featured board games"
        >
        <div 
            class="game-strip marquee-right" 
            aria-hidden="true">
                <div
                    v-for="(game, index) in topRow"
                    :key="`top-${game.id}-${index}`" 
                    class="game-card pa-0" 
                    alt=""
                    decoding="async"
                    > 
                    <BaseImage
                        :src="game.image"
                        :alt="game.title"
                        height="160" 
                        cover
                    />
</div>
        </div>

        <div 
            class="game-strip marquee-left" 
            aria-hidden="true">
                <div 
                    v-for="(game, index) in secondRow"
                    :key="`top-${game.id}-${index}`" 
                    class="game-card pa-0"
                    alt=""
                    decoding="async"
                     > 
                    <BaseImage
                        :src="game.image"
                        :alt="game.title"
                        height="160" 
                        cover
                    />
    </div>
    </div>
</section>
</template>

<script setup>
import { computed} from 'vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import { getLandingGames } from '~/services/landingService'

const games = getLandingGames()

const duplicate = (items) => [
    ...items, 
    ...items
]

const topRow = computed(() => duplicate(games))

const secondRow = computed(() =>
    duplicate([...games].reverse()
    )
)

</script>