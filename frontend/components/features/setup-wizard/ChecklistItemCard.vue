<template>
    <BaseCard
        class="checklist-item"
        :class="{ 'checklist-item--checked' : item.checked }"
        clickable
        role="checkbox"
        :aria-checked="item.checked"
        tabindex="0"
        @click="$emit('toggle', item.id)"
        @keydown.enter.prevent="$emit('toggle', item.id)"
        @keydown.space.prevent="$emit('toggle', item.id)"
    >
        <div class="checklist-item__row">
            <span class="checklist-item__check" aria-hidden="true">
                <v-icon v-if="item.checked" size="16">mdi-check</v-icon>
            </span>

            <div class="checklist-item__body">
                <div class="checklist-item__heading">
                    <h3 class="checklist-item__title">{{  item.title  }}</h3>
                    <span class="checklist-item__category">{{  item.category  }}</span>
                </div>

                <p class="checklist-item__desc">{{ item.description }}</p>

                <div v-if="item.pills?.length" class="checklist-item__pills">
                    <span v-for="pill in item.pills" :key="pill" class="checklist-item__pill">{{  pill  }}</span>
                </div>

                <div v-if="item.customPills?.length" class="checklist-item__pills">
                    <span v-for="p in item.customPills" :key="p.label" class="checklist-item__pill checklist-item__pill-swatch">
                        <span class="checklist-item__swatch" :style="{ backgroundColor: p.color, borderColor: p.border ? 'var(--color-border-strong)' : 'transparent'}" />
                        {{  p.label }}
                    </span>
                </div>

            </div>
        </div>
    </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '~/components/ui/BaseCard.vue'
import type { ChecklistItem } from '~/composables/useSetupWizard'

defineProps<{ item: ChecklistItem }>()
defineEmits<{ (e: 'toggle', id: number): void}>()
</script>