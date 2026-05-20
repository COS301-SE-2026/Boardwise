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