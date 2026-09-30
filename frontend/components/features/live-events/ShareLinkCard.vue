<template>
    <BaseCard>
        <p class="card-title">Walk-In &amp; Share</p>
        <p class="card-meta">Share this link so others can claim the open seat.</p>

        <div class="share-link-row">
            <span class="share-link-row__url">{{ url }}</span>
            <BaseButton size="sm" variant="primary" @click="copy">
                <v-icon start size="14">mdi-content-copy</v-icon>{{ copied ? 'Copied!' : 'Copy' }}
            </BaseButton>
        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({ url: { type: String, required: true } })
const copied = ref(false)

const copy = async () => {
    try {
        await navigator.clipboard.writeText(props.url)
    } catch {}

    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
}
</script>