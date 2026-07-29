<template>
    <v-container class="py-10 text-center">

        <h2>Still need help?</h2>

        <p class="text-medium-emphasis mt-4">
            If you can't find what you're looking for, contact the Boardwise team.
        </p>

        <BaseButton
            class="mt-6"
            color="white"
            href="mailto:support@boardwise.co.za"
        >
            Contact Support
        </BaseButton>

    </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue';

</script>
