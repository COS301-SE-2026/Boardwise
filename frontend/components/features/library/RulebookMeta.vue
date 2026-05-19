<template>
  <div class="meta">

    <p class="category">
      {{ rulebook.category }}
    </p>

    <h1>
      {{ rulebook.title }}
    </h1>

  </div>
</template>

<script setup>
defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category {
  color: #6C3BFF;
  font-weight: 600;
  font-size: 15px;

  text-transform: uppercase;
  letter-spacing: 1px;
}

h1 {
  margin: 0;

  font-size: 52px;
  line-height: 1.1;

  color: #1e1e1e;
}

@media (max-width: 768px) {
  h1 {
    font-size: 38px;
  }
}
</style>