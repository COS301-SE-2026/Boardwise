<template>
  <section class="communities-section">

    <div class="section-heading">
      <SectionTitle
        title="Active Communities"
      />

      <NuxtLink to="/social" class="see-all">
        Explore Communities
        <v-icon size="16">mdi-arrow-right</v-icon>
      </NuxtLink>
    </div>

    <div v-if="communities?.length" class="communities-row">
      <NuxtLink
        v-for="community in communities"
        :key="community.id"
        :to="`/social/community/${community.id}`"
        class="community-pill"
      >
        <BaseAvatar 
          :src="community.image"
          :name="community.name"
          size="sm"
          class="community-avatar"
        />
        
        <div class="pill-text">
          <span class="pill-name">{{ community.name }}</span>
          <span v-if="community.memberCount" class="pill-meta">{{ community.memberCount }} members</span>
        </div>
      </NuxtLink>
    </div>

    <BaseEmptyState
      v-else
      title="No active communities"
      message="Join a community to see it here."
    />

  </section>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({
  communities: {
    type: Array,
    required: true
  }
})

</script>

<style scoped>
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

.pills-row {
  display:        flex;
  gap:            var(--space-3);
  overflow-x:     auto;
  padding-bottom: var(--space-2);
  scrollbar-width: none;
}
.pills-row::-webkit-scrollbar { display: none; }

.community-pill {
  display:        flex;
  flex-direction: row;
  align-items:    center;
  gap:            var(--space-3);
  flex-shrink:    0;
  padding:        var(--space-2) var(--space-4) var(--space-2) var(--space-2);
  background:     var(--alabaster);
  border:         1px solid var(--color-border);
  border-radius:  var(--radius-pill);
  cursor:         pointer;
  transition:     border-color var(--transition-base), box-shadow var(--transition-base);
}
.community-pill:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.community-avatar {
  border: 2px solid var(--color-border);
  flex-shrink: 0;
}

.pill-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.pill-name {
  font-family:   var(--font-body);
  font-size:     var(--fs-small);
  font-weight:   var(--fw-bold);
  color:         var(--color-secondary);
  white-space:   nowrap;
  overflow:      hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.pill-meta {
  font-family: var(--font-body);
  font-size:   11px;
  color:       var(--color-text-muted);
}
</style>