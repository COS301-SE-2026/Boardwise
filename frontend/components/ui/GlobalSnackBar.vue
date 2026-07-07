<template>
    <v-snackbar
        v-model="visible"
        :color="color"
        :timeout="3000" 
        location="bottom right"
        rounded="lg" 
        >
        {{ message }}
        <template #actions>
            <v-btn icon="mdi-close" variant ="text" @click="visible = false"/>
        </template>
    </v-snackbar>
</template>

<script setup>
import { useSnackBar } from '~/composables/useSnackbar';

const { visible, message, color } = useSnackBar()
</script>