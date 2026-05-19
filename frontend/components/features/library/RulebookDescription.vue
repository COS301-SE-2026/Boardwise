<template>
  <div class="description">

    <h3>Description</h3>

    <p>
      {{ rulebook.description }}
    </p>

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
.description {
  background: white;

  padding: 28px;

  border-radius: 24px;

  border: 1px solid #eee;
}

h3 {
  margin-top: 0;
  margin-bottom: 16px;

  font-size: 22px;
}

p {
  margin: 0;

  color: #555;
  line-height: 1.8;
  font-size: 16px;
}
</style>