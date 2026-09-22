<template>
    <PageContainer>
        <v-row class="fill-height" justify="center" align="center">
            <v-col cols="12" sm="8" md="5" lg="4">
                <ResetPassword data-test="reset-password-form" />
            </v-col>
        </v-row>
    </PageContainer>
</template>

<script setup>
import ResetPassword from '~/components/features/auth/ResetPassword.vue';
import PageContainer from '~/components/layout/PageContainer.vue';
</script>