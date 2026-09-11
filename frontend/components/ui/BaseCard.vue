<!-- Base Card for every other Card (Marketplace, Library and Profile Cards) -->
<template> 
    <v-card 
        class="base-card"
        :class="{ 'base-card--clickable' : clickable}"
        rounded="lg"
        v-bind="$attrs"
    >
        <div v-if="$slots.media" class="base-card__media">
            <slot name="media" />
        </div>

        <div class="base-card__body" :class="{ 'base-card__body--flush': flush }">
            <slot />
        </div>

        <div v-if="$slots.actions" class="base-card__actions">
            <slot name="actions" />
        </div>
    </v-card>
</template>

<script setup>
defineProps({
    clickable: { type: Boolean, default: false },
    flush: {type: Boolean, default: false }
})
</script>