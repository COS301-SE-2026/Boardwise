<template>
  <PageContainer>
    <v-container class="fill-height" fluid>
      <v-row justify="center" align="center">
        <v-col cols="12" sm="8" md="5" lg="4">
          <SignUpForm data-test="sign-up-form" />
        </v-col>
      </v-row>
    </v-container>
  </PageContainer>
</template>

<script setup>
import SignUpForm from '~/components/features/auth/SignUpForm.vue'
import PageContainer from '~/components/layout/PageContainer.vue';
</script>