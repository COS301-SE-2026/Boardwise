<template>
  <section class="py-8 section-border" id="voice">
    <SectionTitle 
      title="07 - Voice & Tone" 
      subtitle="Boardwise talks like a knowledgeable friend who loves games - enthusiastic, clear, never condescending. We're playful
      when the moment calls for it and direct when it doesn't."
    />

    <v-row class="mt-4">
        <v-col cols="12" md="6">
            <v-card flat border class="voice-card do pa-4 h-100">
                <div class="voice-card-title do-title mb-3">
                    <v-icon>mdi-close-thick</v-icon>
                    Write like this
                </div>

                <div v-for="line in doExamples" :key="line" class="voice-example">
                    {{ line }}
                </div>
            </v-card>
        </v-col>

        <v-col cols="12" md="6">
            <v-card flat border class="voice-card do pa-4 h-100">
                <div class="voice-card-title dont-title mb-3">
                    <v-icon>mdi-check</v-icon>
                    Not like this
                </div>

                <div v-for="line in doExamples" :key="line" class="voice-example">
                    {{ line }}
                </div>
            </v-card>
        </v-col>
    </v-row>

    <v-row class="mt-2">
        <v-col cols="12" md="6">
            <v-card flat border class="pa-4 h-100">
                <div class="component-card-title mb-2">Personality Traits</div>
                <ul class="trait-list">
                    <li v-for="trait in traits" :key="trait.label">
                        <v-icon :icon="trait.icon" size="18" color="primary" class="trait-icon" />
                        <strong class="trait-label">{{  trait.label  }}</strong> - {{ trait.desc }}
                    </li>
                </ul>
            </v-card>
        </v-col>

        <v-col cols="12" md="6">
            <v-card flat border class="pa-4 h-100">
                <div class="component-card-title mb-2">Error Messages</div>
                <ul class="trait-list">
                    <li v-for="rule in errorRules" :key="rule.text">
                        <span :class="rule.good ? 'rule-good' : 'rule-bad'">{{ rule.good }}</span>
                        {{  rule.text  }}
                    </li>
                </ul>
            </v-card>
        </v-col>
    </v-row>

    <v-row class="mt-2">
        <v-col cols="12">
            <v-card flat border class="mascot-card pa-4 h-100">
                
                <div class="mascot-header mb-3">
                    <img
                        src="/images/cute.svg"
                        alt="Boarley mascot"
                        class="mascot-icon"
                    />
                            
                    <div>
                        <div class="component-card-title">Meet Boarley</div>
                        <div class="mascot-subtitle">Our mascot - a  warm witty boar who shows up in empty states, tooltips and celebratory moments</div>
                    </div>

                </div>

                <v-row>
                    <v-col cols="12" md="6">
                        <div class="mascot-label mb-2">Boarley's personality</div>

                        <ul class="trait-list">
                            <li v-for="trait in mascotTraits" :key="trait.label">
                                <v-icon :icon="trait.icon" size="18" color="primary" class="trait-icon" />
                                <strong class="trait-label">{{  trait.label }}</strong> - {{  trait.desc  }}
                            </li>
                        </ul>
                    </v-col>

                    <v-col cols="12" md="6">
                        <div class="mascot-label mb-2">Boarley in his own words</div>

                        <ul class="trait-list">
                            <li v-for="line in mascotLines" :key="line" class="voice-example mascot-line">
                                {{ line }}
                            </li>
                        </ul>
                    </v-col>
                </v-row>
            </v-card>
        </v-col>
    </v-row>
  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'

const doExamples = [
    'Your rulebook is ready to read.',
    "Catan Night starts in 2 hours - you're in!",
    "You are invited to an event by host (e.g james)"
]

const dontExamples = [
    'The rulebook upload process has been completed successfully.',
    'You have an upcoming event scheduled.',
    'There are 3 marketplace listings matching your criteria.'
]

const traits = [
    { icon: 'mdi-dice-multiple', label: 'Playful', desc: 'We love games and it shows'},
    { icon: 'mdi-book-open-page-variant', label: 'Clear', desc: 'No jargon, no waffle'},
    { icon: 'mdi-account-group', label: 'Welcoming', desc: "Everyone's a board gamer"},
    { icon: 'mdi-arrow-right-bold-circle', label: 'Direct', desc: 'Tell people what to do next'},
    { icon: 'mdi-target', label: 'Specific', desc: 'Names, numbers, real details'}
]

const mascotTraits = [
    { icon: 'mdi-emoticon-happy-outline', label: 'Warm', desc: 'Encouraging, never smug'},
    { icon: 'mdi-lightning-bolt-outline', label: 'Quick-witted', desc: "A light joke, not a bit"},
    { icon: 'mdi-hand-heart-outline', label: 'Loyal', desc: "He's on the player's side, always"},
    { icon: 'mdi-paw', label: 'A little scrappy', desc: 'Boars root around and dig things up - hs is curious and a bit tenacious'}
]

const mascotLines = [
    "Rooted around and found your rulebook - it's ready.",
    "No event on your calender. Would you like to find more events?",
    "Beautiful listing! I'll go tell the others."
]
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}

.voice-card {
    border-radius: var(--radius-md);
}

.voice-card.do {
    border-radius: var(--color-success) !important;
}

.voice-card.dont {
    border-radius: var(--color-error) !important;
}

.voice-card-title {
    font-weight: var(--fw-bold);
}

.do-title {
    color: var(--color-success);
}

.dont-title {
    color: var(--color-error);
}

.voice-example {
    font-size: var(--fs-small);
    color: var(--color-text-muted);
    padding: 6px 0;
    border-bottom: 1px dashed var(--color-border);
}
 
.voice-example:last-child {
  border-bottom: none;
}
 
.component-card-title {
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}
 
.trait-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0;
  margin: 0;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.trait-label {
    color: var(--color-text);
}

.trait-icon {
    margin-right: 4px;
    vertical-align: -3px;
}
 
.rule-good {
  color: var(--color-success);
  font-weight: var(--fw-bold);
  margin-right: 4px;
}
 
.rule-bad {
  color: var(--color-error);
  font-weight: var(--fw-bold);
  margin-right: 4px;
}

.mascot-card {
    border-radius: var(--radius-md);
    border-color: var(--color-primary) !important;
}

.mascot-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.mascot-icon {
    margin-top: 2px;
}

.mascot-subtitle {
    font-size: var(--fs-small);
    color: var(--color-text-muted);
    margin-top: 2px; 
}

.mascot-label {
    font-weight: var(--fw-bold);
    color: var(--color-secondary);
    font-size: var(--fs-small);
}

.mascot-line {
    font-style: italic;
}
</style>