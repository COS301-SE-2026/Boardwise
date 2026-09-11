<template>
  <section class="section">
    <slot />
  </section>
</template>

<style scoped>
.section {
  margin-top: var(--space-8);
}

@media (max-width: 600px) {
  .section {
    margin-top: var(--space-6);
  }
}
</style>