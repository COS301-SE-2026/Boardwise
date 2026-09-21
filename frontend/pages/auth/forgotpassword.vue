<template>
    <PageContainer>
        <v-row class="fill-height" justify="center" align="center">
            <v-col cols="12" sm="8" md="5" lg="4">
                <ForgotPassword data-test="forgot-password-form" />
            </v-col>
        </v-row>
    </PageContainer>
</template>

<script setup>
import ForgotPassword from '~/components/features/auth/ForgotPassword.vue';
import PageContainer from '~/components/layout/PageContainer.vue';
</script>