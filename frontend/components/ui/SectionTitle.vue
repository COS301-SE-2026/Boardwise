<template>
    <div class="section-title">
        <h2>{{ title }}</h2>
        <p>{{ subtitle }}</p>
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
}

.section-title p {
  color: var(--color-text-muted);
  font-size: var(--fs-body-lg);
  margin: 0;
}
</style>