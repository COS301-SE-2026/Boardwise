<template>
  <section
    class="help-ai-section"
    aria-labelledby="help-ai-heading"
  >
    <v-container class="py-10 py-md-14">
      <BaseCard
        color="primary"
        class="pa-8 pa-md-14"
        rounded="xl"
      >
        <div class="help-ai-cta text-center ">
                 <h2  id="help-ai-heading"
                   class="help-ai-cta__title mb-6 " >
                     Stuck on a rule?
                    Ask Boarley.
                 </h2>
            </div>
            
            <p class="help-ai-cta__copy mb-8 text-centre">
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
import BaseCard from '~/components/ui/BaseCard.vue'
const router = useRouter()

</script>