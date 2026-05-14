<template>
    <BaseCard>
        <div class="appearance">
            <h2>Appearance</h2>
            <p>Customise how Boarrdwise looks for you</p>

            <div class="appearance__group">
                <label>Themes</label>
                <div class="appearance__row">
                    <button
                        v-for="theme in themes" :key="t.value"
                        :class="{ active: appearance === t.value }"
                        @click="appearance = t.value"

                        {{ t.label }}
                    >
                    </button>

                </div>
            </div>
            <button @click="emit('save', appearance)">Apply</button>
        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue';
import BaseCard from '@/components/ui/BaseCard.vue';

const emit = defineEmits(['save']);

const themes = [
    { label: 'Light', value: 'light' },
    { label: 'Dark', value: 'dark' },
    { label: 'System', value: 'system' },
]
</script>

<style scoped>
    .appearance {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.appearance__group {
    display: flex;
    flex-direction: column;
    gap: 10px;
}



.appearance__row {
    display: flex;
    gap: 10px;
}

.appearance__row button {
    padding: 6px 16px;
    border: 1px solid #ccc;
    background: none;
    cursor: pointer;
    border-radius: 6px;
}

.appearance__row button.active {
    font-weight: 500;
    border-color: #444;
}
</style>