<template>
  <v-container fluid style="min-height: 100vh; display: flex; align-items: center; justify-content: center;">
    <v-row justify="center" align="center" style="width: 100%;">
      <v-col cols="12" class="text-center">

        <h1 style="font-size: 96px; font-weight: 700; line-height: 1.1;" class="mb-6">
          Boardwise
        </h1>

        <p style="font-size: 24px;" class="text-medium-emphasis mb-12">
          Your board game hub
        </p>

        <div class="d-flex justify-center ga-6">
          <v-btn
            color="primary"
            size="x-large"
            rounded="pill"
            elevation="0"
            style="padding: 0 48px; font-size: 18px; height: 56px;"
            @click="router.push('/auth/signin')"
          >
            Sign In
          </v-btn>

          <v-btn
            variant="outlined"
            size="x-large"
            rounded="pill"
            elevation="0"
            style="padding: 0 48px; font-size: 18px; height: 56px;"
            @click="router.push('/library')"
          >
            Rulebooks
          </v-btn>
        </div>

      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
const router = useRouter()
</script>