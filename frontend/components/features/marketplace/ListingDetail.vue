<template> 
    <div class="detail">

        <div class="image-container">
            <img :src="/games/catan.jpg" />
        </div>

        <div class="info">

            <h1>Catan</h1>

            <p class="price">
                R450
            </p>

            <p>
                Great condition strategy board game. 
            </p>
            
            <BaseButton> 
                Contact Seller
            </BaseButton>

        </div>

    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
</script>

<script scoped>
.detail {
    display: grid;

    grid-template-columns: column:
        repeat(auto-fit, minmax(300px, 1fr));

    gap: 24px;
}

.image-container {
    background: #f4f4f4;
    border-radius: 12px;
    padding: 20px;
}

img { 
    width: 100%;
    object-fit: contain;
}

.price {
    color: #7B3CBF;
    font-size: 24px;
    font-weight: bold;
}

.info {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
</style>