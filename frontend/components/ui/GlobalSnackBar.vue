<template>
    <v-snackbar
        v-model="visible"
        :color="color"
        :timeout="3000" 
        location="bottom right"
        rounded="lg" 
        class="base-snackbar"
        >
        <output
            aria-live="polite"
            aria-atomic="true"
      >
        {{ message }}
        </output>
        <template #actions>
            <v-btn icon="mdi-close" variant ="text" aria-label="Dismiss notification" @click="visible = false"/>
        </template>
    </v-snackbar>
</template>

<script setup>
import { useSnackBar } from '~/composables/useSnackbar';

const { visible, message, color } = useSnackBar()
</script>