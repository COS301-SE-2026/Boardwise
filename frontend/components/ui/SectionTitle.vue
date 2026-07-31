<template>
    <div class="section-title">
        <h1>{{ title }}</h1>
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
  padding: var(--space-12) 0 var(--space-8);
  color: var(--color-primary);
}

.section-title h1 {
  margin: 0;
  color: var(--color-primary);
  font-family: var(--font-display);
  font-size: var(--space-8);
  line-height: 1;
}

.section-title p {
  color: var(--color-text-muted);
  font-size: 1.35rem;
  font-family: var(--font-body);

  margin-top: var(--space-3);
  max-width: 650px;
}
</style>