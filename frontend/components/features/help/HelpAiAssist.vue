<template>
  <section
    class="help-ai-section"
    aria-labelledby="help-ai-heading"
  >
    <v-container class="py-10 py-md-14">
      <BaseCard
        color="primary"
        class="pa-8 pa-md-14"
      >
        <div class="text-center mx-auto">
                 <p  class="cta-title text-h3 font-weight-bold mb-8 " >
                    Stuck on a rule?
                    Ask Boarley.
                </p>
            </div>
            
            <p class="text-body-1 text-white mb-8">
              Choose a rulebook and ask Boarley a question.<br>
              Get answers grounded in the available game rules so
              you can spend less time searching and more time playing.
            </p>

            <div class="d-flex justify-center ga-4 flex-wrap">
              
              <BaseButton
                variant="outlined"
                color="white"
                @click="router.push('/library')"
              >
                <v-icon
                  icon="mdi-book-open-page-variant-outline"
                  class="me-2"
                  aria-hidden="true"
                />

                Browse Rulebooks
              </BaseButton>
            </div>
          </BaseCard>
    </v-container>
  </section>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()

</script>