<template>
  <PageContainer>
      <v-row class="fill-height" justify="center" align="center">
        <v-col cols="12" sm="8" md="5" lg="4">
          <SignUpForm data-test="sign-up-form" />
        </v-col>
      </v-row>
  </PageContainer>
</template>

<script setup>
import SignUpForm from '~/components/features/auth/SignUpForm.vue'
import PageContainer from '~/components/layout/PageContainer.vue';
</script>