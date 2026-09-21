<template>
  <PageContainer>
    <v-row class="fill-height" justify="center" align="center">
      <v-col cols="12" sm="8" md="5" lg="4">
        <SignInForm data-test="sign-in-form" />
      </v-col>
    </v-row>
  </PageContainer>
</template>

<script setup>
import SignInForm from '~/components/features/auth/SignInForm.vue'
import PageContainer from '~/components/layout/PageContainer.vue';
</script>