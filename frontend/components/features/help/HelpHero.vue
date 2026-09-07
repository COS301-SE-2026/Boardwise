<template>
    <section>
    <v-container class="py-12 text-center">
            <div class="help-hero__copy">
                <h1 class="help-hero__title">
                    How can we help?
                </h1>

                <p class="help-hero__description">
                    Find guides, answers and support for using Boardwise.
                </p>

                </div>
    </v-container>
    </section>
</template>
<script setup>

defineProps({
    modelValue: {
        type: String,
        default: ''
    }
})

defineEmits(['update:modelValue'])
</script>