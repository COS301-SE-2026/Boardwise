<template> 
    <section class="py-8 section-border" id="components">
        <SectionTitle
            title="04 - Components"
            subtitle="Reusable primitives built on the tokens above."
        />

        <div class="d-flex flex-column ga-6 mt-4">
            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Buttons</span>
                <div class="d-flex flex-wrap ga-3 mt-2">
                    <BaseButton variant="primary">Primary</BaseButton>
                    <BaseButton variant="secondary">Secondary</BaseButton>
                    <BaseButton variant="accent">Accent</BaseButton>
                    <BaseButton variant="ghost">Ghost</BaseButton>
                    <BaseButton variant="primary" disabled>Disabled</BaseButton>
                </div>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Cards</span>
                <v-row class="mt-2">
                    <v-col cols="12" sm="6">
                        <BaseCard class="pa-6">
                            <h4 class="mb-2">Catan</h4>
                            <p class="card-text">Used across Marketplace, Library and Profile cards.</p>
                        </BaseCard>
                    </v-col>
                </v-row>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Inputs</span>
                <v-row class="mt-2">
                    <v-col cols="12" sm="6">
                        <BaseInput label="Game name" variant="outlined" density="comfortable" />
                    </v-col>

                    <v-col cols="12" sm="6">
                        <BaseInput 
                            label="Game name"
                            variant="outlined"
                            density="comfortable"
                            error 
                            error-messages="This field is required"
                        />
                    </v-col>
                </v-row>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Badges</span>
                <div class="d-flex flex-wrap ga-3 mt-2">
                    <BaseBadge variant="default">Default</BaseBadge>
                    <BaseBadge variant="public">Public</BaseBadge>
                    <BaseBadge variant="private">Private</BaseBadge>
                    <BaseBadge variant="rent">Rent</BaseBadge>
                    <BaseBadge variant="sale">Sale</BaseBadge>
                    <BaseBadge variant="success">Available</BaseBadge>
                    <BaseBadge variant="fire">Hot</BaseBadge>
                    <BaseBadge variant="obsidian">Featured</BaseBadge>
                </div>
            </div>

            <div class="spec-block pd-4">
                <span class="spec-label mb-2">Skeleton Loader</span>
                <div class="d-flex flex-wrap ga-4 mt-2">
                    <BaseSkeleton />
                </div>
            </div>

            <div class="spec-block pd-4">
                <span class="spec-label mb-2">Modal</span>
                <BaseButton variant="primary" class="mt-2" @click="showModal = true">Open Modal</BaseButton>
                <BaseModal v-model="showModal" :max-width="480">
                    <h4 class="mb-2">Invite a friend</h4>
                    <p class="card-text mb-4">Send a link to join your next game night.</p>
                    <div class="d-flex ga-3 justify-end">
                        <BaseButton variant="secondary" @click="showModal = false">Cancel</BaseButton>
                        <BaseButton variant="primary" @click="showModal = false">Send invite</BaseButton>
                    </div>
                </BaseModal>
            </div>
        </div>
    </section>

</template>

<script setup>
import { ref } from 'vue'

import SectionTitle from '../ui/SectionTitle.vue';
import BaseButton from '../ui/BaseButton.vue';
import BaseCard from '../ui/BaseCard.vue';
import BaseInput from '../ui/BaseInput.vue';
import BaseBadge from '../ui/BaseBadge.vue';
import BaseModal from '../ui/BaseModal.vue';
import BaseSkeleton from '../ui/BaseSkeleton.vue';

const showModal = ref(false)
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}
 
.spec-block {
  border-bottom: 1px dashed var(--color-border);
}
 
.spec-label {
  display: block;
  color: var(--color-primary);
  font-weight: var(--fw-bold);
}
 
h4 {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
  color: var(--color-secondary);
}
 
.card-text {
  color: var(--color-text-muted);
  margin: 0;
}
</style>