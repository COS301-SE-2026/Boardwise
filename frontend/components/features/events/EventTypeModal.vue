<template>
    <BaseModal v-model="open" title="Create an event" :max-width="560">
        <p class="card-meta" style="margin-bottom: var(--space-4)">
            What kind of event are you hosting?
        </p>

        <div class="settings-choice-grid settings-choice-grid--two">
            <button
                type="button"
                class="settings-choice-card"
                data-test="event-type-normal"
                @click="choose('normal')"
            >
                <span class="settings-choice-card__icon"><v-icon size="22">mdi-calendar-plus</v-icon></span>
                <span class="settings-choice-card__content">
                    <span class="settings-choice-card__title">Scheduled Event</span>
                    <span class="settings-choice-card__description">Plan ahead with a date, time, location and RSVPs.</span>
                </span>
            </button>

            <button
                type="button"
                class="settings-choice-card"
                data-test="event-type-live"
                @click="choose('live')"
            >
                <span class="settings-choice-card__icon"><v-icon size="22">mdi-broadcast</v-icon></span>
                <span class="settings-choice-card__content">
                    <span class="settings-choice-card__title">Live Table</span>
                    <span class="settings-choice-card__description">Playing now? Open seats to nearby players instantly.</span>
                </span>
            </button>
        </div>
    </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'

const open = defineModel({ type: Boolean })
const emit = defineEmits(['select'])

const choose = (type) => {
    open.value = false 
    emit('select', type)
}
</script>