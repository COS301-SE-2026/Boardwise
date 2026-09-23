<template>
    <BaseCard class="active-setup-banner">
        <div class="active-setup-banner__row">
            <div class="active-setup-banner__info">
                <BaseImage :src="coverImage" :alt="game.title" height="80px" width="80px" fit="cover" rounded="lg" />

                <div>
                    <div class="active-setup-banner__heading">
                        <h3 class="card-title">{{  game.title  }}</h3>
                        <BaseBadge variant="neutral">Step {{  game.step  }} of {{  game.totalSteps  }}</BaseBadge>
                    </div>

                    <div class="active-setup-banner__progress">
                        <div class="active-setup-banner__track">
                            <div class="active-setup-banner__fill" :style="{ width: progressPercent + '%' }" />
                        </div>
                    </div>

                    <span class="card-meta">{{  progressPercent }}%</span>
                </div>
            </div>

            <div class="active-setup-banner__actions">
                <BaseButton variant="secondary" @click="$emit('restart')">Restart</BaseButton>
                <BaseButton @click="$emit('resume')">Resume Setup</BaseButton>
            </div>
        </div>

    </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import type { ActiveSetup } from '~/composables/useSetupWizard'

const props = defineProps<{ game: ActiveSetup }>()
defineEmits<{ (e: 'resume'): void; (e: 'restart'): void}>()

const coverImage = computed(() => props.game.coverImage || '/images/BoarleySide.svg')
const progressPercent = computed(() => Math.round((props.game.step / props.game.totalSteps) * 100))
</script>