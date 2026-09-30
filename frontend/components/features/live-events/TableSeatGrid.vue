<template>
    <div class="table-seat-grid">
        <BaseCard v-for="s in seats" :key="s.seat" class="seat-card" :class="{ 'seat-card--open' : s.status === 'OPEN' }">
            <template v-if="s.status !== 'OPEN'">
                <div class="seat-card__row">
                    <BaseAvatar :name="s.user?.username" size="sm" />

                    <div class="seat-card__info">
                        <p class="card-subtitle" style="margin: 0">{{ s.user?.username }}</p>
                        <BaseBadge v-if="s.isHost" variant="primary" size="x-small">Host</BaseBadge>
                        <!-- <span v-else class="card-meta">{{ statusLabel(s.status) }}</span> -->
                    </div>
                </div>

                <p class="card-meta">Seat {{ s.seat }}</p>
            </template>

            <template v-else>
                <v-icon size="28" color="var(--color-primary)">mdi-plus-circle-outline</v-icon>
                <p class="card-subtitle" style="margin: 0">Seat {{ s.seat }} open</p>
                <BaseButton variant="accent" size="sm" @click="$emit('claim')">
                    Claim seat
                </BaseButton>
            </template>
        </BaseCard>
    </div>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({ seats: { type: Array, required: true } })
defineEmits(['claim'])

const statusLabel = (status) => ({ SEATED: 'At table', EN_ROUTE: 'En route'}[status] ?? status)
</script>