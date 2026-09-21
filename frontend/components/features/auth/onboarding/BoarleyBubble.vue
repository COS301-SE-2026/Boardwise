<template>
    <div class="boarley-bubble">
        <BaseAvatar src="/images/Boarley.svg" alt="Boarley" size="xl" />
        <div class="boarley-bubble_text">
            <slot />
        </div>
    </div>
</template>

<script setup>
    import BaseAvatar from '~/components/ui/BaseAvatar.vue'
</script>