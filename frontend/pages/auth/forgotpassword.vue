<template>
    <v-container class="fill-height" fluid>
        <v-row justify="center" align="center">
            <v-col cols="12" sm="8" md="5" lg="4">
                <ForgotPassword data-test="forgot-password-form" />
            </v-col>
        </v-row>
    </v-container>
</template>

<script setup>
import ForgotPassword from '~/components/features/auth/ForgotPassword.vue';
</script>