<template>
    <div class="section-title">
        <h2>{{ title }}</h2>
        <p v-if="subtitle">{{ subtitle }}</p>
    </div>
</template>

<script setup> 
defineProps({ 
    title: String,
    subtitle: String
})
</script>

<style scoped>
.section-title {
  padding-top: var(--space-10);
  margin-bottom: var(--space-8);
}

.section-title h2 {
  color: var(--color-primary);
  margin-bottom: var(--space-1);
  font-family: var(--font-display);
  font-size: var(--fs-h2);
}

.section-title p {
  color: var(--color-text-muted);
  font-size: var(--fs-body-lg);
  font-family: var(--font-body);
  margin: 0;
}
</style>