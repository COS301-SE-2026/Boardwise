<template>
    <div class="tabs">

        <button
            v-for="tab in tabs"
            :key="tab"
            :class="{ active: selectedTab === tab}"
            @click="selectedTab=tab"
        >
            {{ tab }}
        </button>

    </div>
</template>

<script setup>
const tabs = [
    'Trending',
    'Nearby',
    'New'
]

const selectedTab = ref('Trading')
</script>

<style scoped>
.tabs {
    display: flex;
    gap: 20px;
    overflow-x: auto;
}

button {
    padding: 10px 16px;
    border: none;
    border-radius 20px;
    background: #eee;
    cursor: pointer;
    white-space: nowrap;
}

.active {
    background: #7B2CBF;
    color: white;
}
</style>