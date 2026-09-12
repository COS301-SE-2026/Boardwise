<template>
    <div class="boarley-state boarley-state__loading" :class="{ 'boarley-state__inline' :inline }">
        <BaseImage 
            :src="mascotSrc"
            :alt="tab === 'Web' ? 'Boarley searching the web' : 'Boarley searching listings'"
            :height="inline ? '32px' : '140px'"
            :width="inline ? '32px' : '140px'"
            fit="contain"
            class="boarley-mascot"
            :class="{ 'boarley-mascot__sm': inline }"
        />

        <p class="boarley-state__title" :class="{ 'text-body-2': inline }">
            {{  message  }}
        </p>

        <v-progress-circular
            v-if="inline"
            indeterminate
            color="primary"
            size="20"
            width="2"
            class="mt-1"
        />

        <div v-else class="boarley-dots" aria-hidden="true">
            <span /><span /><span />
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import BaseImage from '~/components/ui/BaseImage.vue';

const props = defineProps ({
    tab: { type: String, default: 'Community Listings' },
    inline: { type: Boolean, default: false },
    mascotSrc: { type: String, default: '/images/Boarley_cute.svg' }
})

const messages = { 
    'Community Listings': {
        initial: [
            "Boarley's sniffing out board games...",
            'Rummaging through the shelves...',
            'Boarley is on the hunt...'
        ],
        more: 'Fetching more listings...'
    },
    'Web': {
        initial: [
            'Boarley is scouring the web for deals...',
            'Checking in with the retailers...',
            'Boarley is out shopping for you...'
        ],
        more: 'Loading more results...'
    }
}

let messageIndex = 0

const message = computed(() => {
  const set = messages[props.tab] ?? messages['Community Listings']
  if (props.inline) return set.more
  const options = set.initial
  const msg = options[messageIndex % options.length]
  messageIndex++
  return msg
})
</script>

<style scoped>
.boarley-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 12px;
  padding: 48px 24px;
  width: 100%;
  min-height: 40vh;
}

.boarley-state__inline {
  flex-direction: row;
  padding: 16px;
  min-height: unset;
  gap: 10px;
}

.boarley-mascot {
  animation: boarley-bob 2.2s ease-in-out infinite;
}

.boarley-mascot__sm {
  animation-duration: 1.4s;
}

.boarley-state__title {
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface));
  opacity: 0.85;
}

.boarley-dots {
  display: flex;
  gap: 6px;
}

.boarley-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgb(var(--v-theme-primary));
  animation: boarley-pulse 1.2s ease-in-out infinite;
}

.boarley-dots span:nth-child(2) { animation-delay: 0.15s; }
.boarley-dots span:nth-child(3) { animation-delay: 0.3s; }

@keyframes boarley-bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes boarley-pulse {
  0%, 100% { opacity: 0.3; transform: scale(0.85); }
  50% { opacity: 1; transform: scale(1); }
}
</style>