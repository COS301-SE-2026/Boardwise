<template>
  <div class="actions">

    <BaseButton @click="goRead">
      Read Rulebook
    </BaseButton>

    <BaseButton variant="secondary">
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

<style scoped>
.actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .actions {
    flex-direction: column;
  }
}
</style>