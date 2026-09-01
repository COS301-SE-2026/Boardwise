<template>
    <section>
    <v-container class="py-12 text-center">
        <h1>Help Centre</h1>
            <div class="help-hero__copy">
                <p class="help-hero__eyebrow">
                    Boardwise Support
                </p>

                <h1 class="help-hero__title">
                    How can we help?
                </h1>

                <p class="help-hero__description">
                    Find guides, answers and support for using Boardwise.
                </p>

                <div class="help-hero__search">
                    <BaseSearch
                        :model-value="modelValue"
                        placeholder="Search for help..."
                        aria-label="Search Boardwise help"
                        @update:model-value="$emit('update:modelValue', $event)"
                    />
                </div>

                <p class="help-hero__hint">
                    Try searching for communities, events, marketplace,
                    rulebooks or privacy.
                </p>
            </div>
    </v-container>
    </section>
</template>
<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'

efineProps({
    modelValue: {
        type: String,
        default: ''
    }
})

defineEmits(['update:modelValue'])
</script>