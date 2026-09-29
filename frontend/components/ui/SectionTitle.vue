<template>
    <div class="ui-section-title">
        <h1>{{ title }}</h1>
        <p v-if="subtitle">{{ subtitle }}</p>
    </div>
</template>

<script setup> 
defineProps({ 
    title: {
      type: String,
      required: true
    },
    subtitle: String
})
</script>
