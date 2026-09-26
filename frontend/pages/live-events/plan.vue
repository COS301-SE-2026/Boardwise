<template>
    <PageContainer>
        <Navbar />
        <BaseBackButton :to="'/events'">Events</BaseBackButton>

        <h1 style="margin-top: 12px;">Plan a Live Game Night</h1>
        <p class="card-meta">Set up you rtable and open seats to nearby players.</p>

        <form class="plan-live-form" @submit.prevent="handleSubmit">
            <BaseInput v-model="form.name" label="Event title" placeholder="e.g. Catan Marathon" />
            <BaseInput v-model="form.game" label="Game" placeholder="e.g. Catan" />
            <BaseInput v-model="form.venue" label="Venue" placeholder="e.g. Tabletop Tavern Hatfield" />

            <div class="plan-live-form__row">
                <BaseInput v-model="form.table" label="Table" placeholder="e.g. Table 4" />
                <BaseInput v-model.number="form.capacity" label="Capacity" type="number" />
            </div>

            <BaseButton type="submit" :loading="isLoading" :disabled="!isValid">
                <v-icon start>mdi-play-circle</v-icon>Launch Live Event
            </BaseButton>
        </form>
    </PageContainer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

import { useLiveEvents } from '~/composables/useLiveEvents'
import { useSnackBar } from '~/composables/useSnackbar'

definePageMeta({
    middleware: 'auth'
})

const router = useRouter()
const { createLiveEvent, isLoading } = useLiveEvents()
const { show } = useSnackBar()

const form = ref({ name: '', game: '', venue: '', table: '', capacity: 4 })
const isValid = computed(() => form.value.name && form.value.game && form.value.venue && form.value.table)

const handleSubmit = async () => {
  const event = await createLiveEvent(form.value)
  show('Live table launched!', 'success')
  router.push(`/live-events/${event.id}`)
}
</script>