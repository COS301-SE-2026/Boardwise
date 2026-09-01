<template>
  <v-container class="fill-height" fluid>
    <v-row justify="center" align="center">
      <v-col cols="12" sm="9" md="7" lg="5" xl="4">
        <SignInForm data-test="sign-in-form" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import SignInForm from '~/components/features/auth/SignInForm.vue'
</script>