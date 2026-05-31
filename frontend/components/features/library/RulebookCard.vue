<template> 
    <BaseCard class="card" @click="openBook"> 

        <BaseImage :src="rulebook.image" :alt="rulebook.gameName" height="200px" fit="cover" />

        <div class="pa-3">
          <p class="text-body-2 font-weight-bold mb-1">
            {{ rulebook.gameName }}
          </p>

          <p class="text-caption text-medium-emphasis mb-0">
            {{ rulebook.edition }}
          </p>
        </div>
        
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    rulebook: Object
})

const openBook = () => {
    navigateTo(`/library/${props.rulebook.id}`)
}
</script>

<style scoped>
.card {
  cursor: pointer;
  overflow: hidden;
  padding: 0;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
}
</style>