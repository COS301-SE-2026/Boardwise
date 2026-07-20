<template>
  <div class="mt-6">

    <div class="d-flex justify-space-between align-center mb-3">
      <h3 class="section-title ma-0">Communities</h3>
      <NuxtLink to="/community" class="see-all">›</NuxtLink>
    </div>

    <div class="cards-row">
      <NuxtLink
        v-for="community in communities"
        :key="community.id"
        :to="`/community/${community.id}`"
        class="community-card text-decoration-none"
      >
        <v-avatar size="64" rounded="lg" class="community-avatar">
          <v-img :src="community.image" :alt="community.name" cover />
        </v-avatar>
        <p class="card-name">{{ community.name }}</p>
      </NuxtLink>
    </div>

  </div>
</template>

<script setup>
// import { communities } from '~/services/mockData/communities'

defineProps({
  communities: {
    type: Array,
    required: true
  }
})

</script>

<style scoped>
.section-title {
  font-family: var(--font-display);
  font-size:   var(--fs-h4);
  font-weight: var(--fw-regular);
  color:       var(--color-secondary);
}

.see-all {
  font-family:     var(--font-body);
  font-size:       var(--fs-body-lg);
  font-weight:     var(--fw-bold);
  color:           var(--color-primary);
  text-decoration: none;
}
.see-all:hover {
  color: var(--color-primary-hover);
}

.cards-row {
  display:        flex;
  gap:            var(--space-3);
  overflow-x:     auto;
  padding-bottom: var(--space-2);
  scrollbar-width: none;
}
.cards-row::-webkit-scrollbar { display: none; }

.community-card {
  display:        flex;
  flex-direction: column;
  align-items:    center;
  gap:            var(--space-2);
  flex-shrink:    0;
  cursor:         pointer;
}

.community-avatar {
  border:     2px solid var(--color-border);
  transition: border-color var(--transition-base);
}
.community-card:hover .community-avatar {
  border-color: var(--color-primary);
}

.card-name {
  font-family:   var(--font-body);
  font-size:     var(--fs-small);
  color:         var(--color-text-muted);
  text-align:    center;
  margin:        0;
  max-width:     64px;
  white-space:   nowrap;
  overflow:      hidden;
  text-overflow: ellipsis;
}
</style>