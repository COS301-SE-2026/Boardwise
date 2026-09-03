<template>
  <section
    class="help-ai-section"
    aria-labelledby="help-ai-heading"
  >
    <v-container class="py-10 py-md-14">
      <v-sheet
        rounded="xl"
        color="primary"
        class="pa-8 pa-md-14 overflow-hidden position-relative hero-cta"
      >
        <v-row justify="center">
          <v-col
            cols="12"
            md="10"
            lg="8"
            class="text-center"
          >
            <div class="d-flex align-center flex-wrap ga-2 mb-2">
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
          </v-col>
        </v-row>
      </v-sheet>
    </v-container>
  </section>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()

const openRulebookHelp = (question = '') => {
    router.push({
        path: '/rag',
        query: question
            ? {
                ask: question
            }
            : {
                ask: 'true'
            }
    })
}
</script>