<template> 
    <BaseButton 
        data-test="ai-floating-button"
        class="ai-fab"
        icon
        size="56"
        aria-label="Ask Boarley about this rulebook"
        @click="$emit('click')"
    >
       <BaseAvatar src="/images/Boarley_cute.svg" alt="Boarley" size="sm" />
    </BaseButton>
</template>

<script setup>
import BaseAvatar from '../ui/BaseAvatar.vue'
import BaseButton from '../ui/BaseButton.vue';

defineEmits(['click'])
</script>