<template>
    <PageContainer>
        <Navbar />

        <div style="max-width: 760px; margin: 0 auto; width: 100%;">
            <BaseBackButton :to="'/events'">Events</BaseBackButton>

            <div style="margin-top: var(--space-4); margin-bottom: var(--space-6);">
                <span class="onboarding-eyebrow" style="display: inline-block; margin-bottom: 6px;">
                    Host a tabletop session
                </span>
                <h1 style="margin: 4px 0 0;">Plan a live game night</h1>
                <p class="card-meta" style="font-size: var(--fs-body-lg); margin-top: var(--space-2);">
                    Set up your table and open seats to nearby community players in minutes.
                </p>
            </div>

            <form class="live-form-card" @submit.prevent="handleSubmit">
                <!-- 1. Basics -->
                <section class="live-form-section">
                    <div class="live-form-section__title">
                        <span class="live-form-section__dot" />
                        1. Event Basics
                    </div>

                    <BaseInput v-model="form.name" label="Event title" placeholder="e.g. Catan Marathon" />
                    <BaseInput v-model="form.game" label="Game" placeholder="e.g. Catan" />

                    <div>
                        <p class="card-subtitle" style="margin-bottom: var(--space-2)">Session Format</p>
                        <div class="settings-choice-grid settings-choice-grid--two">
                            <button
                                type="button"
                                class="settings-choice-card settings-choice-card--compact"
                                :class="{ 'settings-choice-card--selected': form.format === 'in-person' }"
                                @click="form.format = 'in-person'"
                            >
                                <span class="settings-choice-card__icon"><v-icon size="20">mdi-storefront-outline</v-icon></span>
                                <span class="settings-choice-card__content">
                                    <span class="settings-choice-card__title">In-Person Venue</span>
                                    <span class="settings-choice-card__description">Local cafe, tabletop lounge, or home</span>
                                </span>
                            </button>

                            <button
                                type="button"
                                class="settings-choice-card settings-choice-card--compact"
                                :class="{ 'settings-choice-card--selected': form.format === 'virtual' }"
                                @click="form.format = 'virtual'"
                            >
                                <span class="settings-choice-card__icon"><v-icon size="20">mdi-devices</v-icon></span>
                                <span class="settings-choice-card__content">
                                    <span class="settings-choice-card__title">Online / Virtual</span>
                                    <span class="settings-choice-card__description">Tabletop Simulator, BGA, Discord</span>
                                </span>
                            </button>
                        </div>
                    </div>

                    <BaseInput 
                        v-model="form.venue"
                        :label="form.format === 'virtual' ? 'Platform link / invite' : 'Venue name & table details'"
                        :placeholder="form.format === 'virtual' ? 'e.g. discord.gg/boardwise' : 'e.g. Tabletop Tavern Hatfield'"
                    />

                    <BaseInput v-model="form.table" label="Table" placeholder="e.g. Table 4" />
                </section>

                <section class="live-form-section">
                    <div class="live-form-section__title">
                        <span class="live-form-section__dot" />
                        2. Timing &amp; Readiness
                    </div>

                    <div>
                        <p class="card-subtitle" style="margin-bottom: var(--space-2);">
                            When is this event happening?
                        </p>
                        <div class="live-form-toggle-group">
                            <button 
                                type="button"
                                class="live-form-toggle"
                                :class="{ 'live-form-toggle--active': form.timing === 'now' }"
                                @click="form.timing = 'now'"
                            >
                                <v-icon size="14">mdi-record-circle</v-icon> Start  immediately
                            </button>

                            <button 
                                type="button"
                                class="live-form-toggle"
                                :class="{ 'live-form-toggle--active': form.timing === 'later' }"
                                @click="form.timing = 'later'"
                            >
                                <v-icon size="14">mdi-clock-outline</v-icon> Schedule for later
                            </button>
                        </div>
                    </div>

                    <div v-if="form.timing === 'later'" class="d-flex ga-3">
                        <BaseInput 
                            v-model="form.date" 
                            label="Date" type="date" 
                        />
                        
                        <BaseInput 
                            v-model="form.time" 
                            label="Start time" 
                            type="time" 
                        />
                    </div>

                    <div>
                        <p class="card-subtitle" style="margin-bottom: var(--space-2);">
                           Estimated Duration
                        </p>
                        <div class="settings-choice-grid">
                            <button 
                                v-for="d in durationOptions"
                                :key="d.value"
                                type="button"
                                class="settings-choice-card settings-choice-card--compact"
                                :class="{ 'settings-choice-card--selected': form.duration === d.value }"
                                @click="form.duration = d.value"
                            >
                                <span class="settings-choice-card__content">
                                    <span class="settings-choice-card-title">{{ d.label }}</span>
                                    <span class="settings-choice-card__description">{{ d.desc }}</span>
                                </span>
                            </button>
                        </div>
                    </div>
                </section>

                <section class="live-form-section">
                    <div class="live-form-section__title">
                        <span class="live-form-section__dot" />
                        3. Capacity &amp; Table Tone
                    </div>

                    <div>
                        <p class="card-subtitle" style="margin-bottom: var(--space-2);">
                            Max Table Capacity
                        </p>
                        <p class="card-meta" style="margin-bottom: var(--space-2)">Total seats available (incl. host)</p>
                        <div class="capacity-stepper">
                            <BaseButton 
                                icon
                                size="36"
                                variant="text"
                                @click="adjustCapacity(-1)"
                            >
                                <v-icon size="18">mdi-minus</v-icon>
                            </BaseButton>

                            <span class="capacity-stepper__value">{{ form.capacity }}</span> 
                            <BaseButton
                                icon
                                size="36"
                                variant="text"
                                @click="adjustCapacity(1)"
                            >
                               <v-icon size="18">mdi-plus</v-icon>
                            </BaseButton>
                        </div>
                    </div>

                    <div>
                        <p class="card-subtitle" style="margin-bottom: var(--space-2);">
                           Table Tone &amp; Style
                        </p>
                        <div class="settings-choice-grid">
                            <button 
                                v-for="s in styleOptions"
                                :key="s.value"
                                type="button"
                                class="settings-choice-card settings-choice-card--compact"
                                :class="{ 'settings-choice-card--selected': form.style === s.value }"
                                @click="form.style = s.value"
                            >
                                <span class="settings-choice-card__content">
                                    <span class="settings-choice-card-title">{{ s.label }}</span>
                                    <span class="settings-choice-card__description">{{ s.desc }}</span>
                                </span>
                            </button>
                        </div>
                    </div>
                </section>

                <section class="live-form-section">
                    <div class="live-form-section__title">
                        <span class="live-form-section__dot" />
                        4. Privacy &amp; Seating
                    </div>

                    <div class="settings-choice-grid settings-choice-grid--two">
                        <button 
                            type="button"
                            class="settings-choice-card settings-choice-card--compact"
                            :class="{ 'settings-choice-card--selected': form.privacy === 'public' }"
                            @click="form.privacy = 'public'"
                        >
                            <span class="settings-choice-card__content">
                                <span class="settings-choice-card-title">Public</span>
                                <span class="settings-choice-card__description">Listed on Live Events Hub for nearby players.</span>
                            </span>
                        </button>

                        <button 
                            type="button"
                            class="settings-choice-card settings-choice-card--compact"
                            :class="{ 'settings-choice-card--selected': form.privacy === 'private' }"
                            @click="form.privacy = 'private'"
                        >
                            <span class="settings-choice-card__icon"><v-icon size="20">mdi-lock-outline</v-icon></span>
                            <span class="settings-choice-card__content">
                                <span class="settings-choice-card-title">Private / Friends Only</span>
                                <span class="settings-choice-card__description">Accessible via direct share link only.</span>
                            </span>
                        </button>
                    </div>

                    <div class="d-flex align-center justify-space-between" style="padding: var(--space-3) 0;">
                        <div>
                            <p class="card-subtitle" style="margin: 0;">
                                Auto-approve RSVPs
                            </p>

                            <p class="card-meta">
                                Guests take open seats immediately, no host approval needed.
                            </p>
                        </div>

                        <v-switch v-model="form.autoApprove" color="var(--color-primary)" hide-details density="compact" />
                    </div>
                </section>

                <div class="live-form-footer">
                    <BaseBackButton :to="'/events'" variant="text">Cancel</BaseBackButton>

                    <BaseButton type="submit" :loading="submitting" :disabled="!isValid">
                        <v-icon start>{{ form.timing === 'later' ? 'mdi-calendar-plus' : 'mdi-play-circle' }}</v-icon>
                        {{ form.timing === 'later' ? 'Schedule Event' : 'Launch Live Event' }}
                    </BaseButton>
                </div>
            </form>

            <div class="live-form-hint">
                <span class="live-form-hint__icon"><v-icon size="18">mdi-lightbulb-outline</v-icon></span>
                <div>
                    <p class="card-subtitle" style="margin: 0;">Host tip</p>
                    <p class="card-meta">
                        Public sessions fill up faster when your venue name includes a landmark or table number. 
                    </p>
                </div>
            </div>
        </div>
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
import { useEvents } from '~/composables/useEvents'
import { useBoardGames } from '~/composables/useBoardGames'
import { useSnackBar } from '~/composables/useSnackbar'

definePageMeta({
    middleware: 'auth'
})

const router = useRouter()

const { createEvent } = useEvents()
const { games: searchedGames, searchGames} = useBoardGames()
const { createLiveEvent, isLoading } = useLiveEvents()
const { show } = useSnackBar()

const form = ref({ name: '', game: '', venue: '', table: '', capacity: 4 , format: 'in-person', timing: 'now', date: '', time: '', duration: 'standard', style: 'casual', privacy: 'public', autoApprove: true})
const submitting = ref(false)

const adjustCapacity = (delta) => {
    form.value.capacity = Math.max(3, Math.min(12, form.value.capacity + delta))
}
const isValid = computed(() => {
    const f = form.value
    const base = f.name && f.game && f.venue && f.table
    return f.timing === 'later' ? base && f.date && f.time : base
})

const DURATION_HOURS = { short: 2, standard: 4, marathon: 6 }

const addHours = (time, hours) => {
    const [h,m] = time.split(':').map(Number)
    const end = Math.min(h + hours, 23)
    return `${String(end).padStart(2, '0')}:${end === 23 && h + hours > 23 ? '59' : String(m).padStart(2, '0')}`
}

const handleSchedule = async () => {
    const f = form.value

    await searchGames(f.game)
    const match = searchedGames.value?.[0]

    const eventInfo = {
        name: f.name,
        description: `${f.game} · ${f.style} table · ${f.capacity} seats` + (f.format === 'virtual' ? ` · Join: ${f.venue}` : ` · ${f.table}`), date: f.date,
        startTime: `${f.time}:00`,
        endTime:  `${addHours(f.time, DURATION_HOURS[f.duration] ?? 4)}:00`,
        location: f.format === 'virtual' ? 'Online' : `${f.venue}, ${f.table}`,
        visibility: f.privacy.toUpperCase(),
        games: match ? [match.id] : []
    }

    await createEvent(eventInfo, null)
    show('Your event is scheduled. Game on!', 'success')
    router.push('/events')
}

const handleSubmit = async () => {
    if (submitting.value || !isValid.value) return
    submitting.value = true

    try { 
        if (form.value.timing === 'later') {
            await handleSchedule()
        } else {
            const event = await createLiveEvent(form.value)
            show('Live table launched!', 'success')
            router.push(`/live-events/${event.id}`)
        }
    } catch (err) {
        show(err?.data?.message || 'Something went wrong. Please try again.', 'error')
    } finally {
        submitting.value = false
    }
}

const durationOptions = [
    { value: 'short', label: '1–2 Hours', desc: 'Quick game / filler' },
    { value: 'standard', label: '2–4 Hours', desc: 'Standard Euro/Strategy' },
    { value: 'marathon', label: '4+ Hours', desc: 'Epic or Campaign' }
]

const styleOptions = [
    { value: 'casual', label: 'Casual / Social', desc: 'Relaxed fun' },
    { value: 'learn', label: 'Learn to Play', desc: 'Beginners welcome' },
    { value: 'competitive', label: 'Competitive', desc: 'Deep strategy' }
]
</script>