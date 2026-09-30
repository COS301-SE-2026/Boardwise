<template>
    <div v-if="count > 0" class="live-events-banner">
        <div class="live-events-banner__info">
            <div class="live-events-banner__icon"><v-icon>mdi-table-furniture</v-icon></div>

            <div>
                <p class="card-subtitle" style="margin: 0">{{ count }} live table{{ count > 1 ? 's' : '' }} rolling dice right now</p>
                <p class="card-meta" style="margin: 0">Quick-join open seats in progress</p>
            </div>
        </div>

        <BaseButton variant="secondary" size="sm" @click="$emit('view')">
            View Live Tables <v-icon end size="16">mdi-arrow-right</v-icon>
        </BaseButton>
    </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({ count: { type: Number, default: 0 } })
defineEmits(['view'])
</script>