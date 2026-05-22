<template>
  <div class="d-flex flex-wrap ga-4">
    <BaseButton @click="goRead">
      <v-icon start>
        mdi-book-open-variant
      </v-icon>
      Read Rulebook
    </BaseButton>

    <BaseButton variant="secondary" @click="router.push('/marketplace')">
      <v-icon start>
        mdi-store
      </v-icon>
      Marketplace
    </BaseButton>
  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()

const props = defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})

const goRead = () => {
  router.push(`/rulebook/read/${props.rulebook.id}`)
}
</script>