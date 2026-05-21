<template>
  <v-container class="fill-height" fluid>
    <v-row justify="center" align="center">
      <v-col cols="12" class="text-center">
        <h1 class="text-h2 font-weight-bold mb-4">Boardwise</h1>
        <p class="text-h6 text-medium-emphasis mb-8">Your board game hub</p>
        <div class="d-flex justify-center gap-4">
          <BaseButton @click="router.push('/auth/signin')">Sign In</BaseButton>
          <BaseButton variant="secondary" @click="router.push('/library')">Rulebooks</BaseButton>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
const router = useRouter()
</script>