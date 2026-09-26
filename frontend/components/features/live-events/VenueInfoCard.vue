<template>
    <BaseCard>
        <p class="card-title">Venue &amp; Location</p>
        <div class="venue-info-card__row">
            <div class="venue-info-card__icon">
                <v-icon size="18">mdi-map-marker</v-icon>
            </div>
            <div>
                <p class="card-meta" style="margin: 0">Venue</p>
                <p class="card-subtitle" style="margin: 0">{{ venue }}</p>
            </div>
        </div>

        <div class="venue-info-card__row">
            <div class="venue-info-card__icon">
                <v-icon size="18">mdi-table-furniture</v-icon>
            </div>
            <div>
                <p class="card-meta" style="margin: 0">Table</p>
                <p class="card-subtitle" style="margin: 0">{{ table }}</p>
            </div>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

defineProps({ venue: String, table: String })
</script>