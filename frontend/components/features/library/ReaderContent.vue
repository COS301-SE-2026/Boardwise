<template> 
    <BaseCard>
        <div class="reader">
            <h1>Catan Rules</h1>

            <p> 
                Setup the board by placing the hexagonal tiles randomly.
            </p>

            <p>
                Each player starts with two settlements and two roads.
            </p>

            <p>
                Players collect resources to expand their settlements and earn victory points
            </p>

            <p>
                The first player to reach 10 victory points wins the game.
            </p>

        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
</script>

<style scoped>
.reader {
    max-width: 900px;
    margin: 0 auto;

    display: flex;
    flex-direction: column;

    gap: 20px;
    line-height: 1.8;
}

h1 {
    margin-bottom: 12px;
}

p {
    color: #444;
}
</style>