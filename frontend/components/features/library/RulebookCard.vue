<template> 
    <BaseCard class="card" @click="openBook"> 

        <div class="image-wrapper">
            <img :src="rulebook.image" :alt="rulebook.title" />
        </div>
    
        <div class="content">

            <h3>{{ rulebook.title }}</h3>
            <p>{{ rulebook.category }}</p>
        </div>
        
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
    rulebook: Object
})

const openBook = () => {
    navigateTo('/library/${props.rulebook.id}')
}
</script>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
  transition: 0.2s;
  min-width: 180px;
  max-width: 180px;
  padding: 0;
  overflow: hidden;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.image-wrapper {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content {
  padding: 12px 16px 16px;
}

h3 { margin: 0; font-size: 14px; }

p {
  margin-top: 4px;
  color: #777;
  font-size: 12px;
}
</style>