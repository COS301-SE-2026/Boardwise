<template> 
    <div class="boarley-fab">
        <transition name="boarley-fab-bubble">
            <button 
                v-if="showBubble"
                type="button"
                class="boarley-bubble_text boarley-fab__bubble"
            >
                Ask Boarley a question!
            </button>
        </transition>

        <BaseButton 
            data-test="ai-floating-button"
            class="boarley-fab__button"
            :class="{ 'boarley-fab__button--wiggle': wiggle }"
            icon
            size="60"
            aria-label="Ask Boarley about this rulebook"
            @click="handleClick"
            @mouseenter="playWiggle"
            @animatonend="wiggle = false"
        >
            <BaseAvatar src="/images/Boarley_cute.svg" alt="Boarley" size="smd" />
        </BaseButton>
    </div>
        
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import BaseAvatar from '../ui/BaseAvatar.vue'
import BaseButton from '../ui/BaseButton.vue';

const emit = defineEmits(['click'])

const showBubble = ref(false)
const wiggle = ref(false)

let showTimer = null
let hideTimer = null

const playWiggle = () => {
    wiggle.value = false
    requestAnimationFrame(() => { wiggle.value = true })
}

const handleClick = () => {
    showBubble.value = false
    emit('click')
}

onMounted(() => {
    showTimer = setTimeout(() => {
        showBubble.value = true
        wiggle.value = true

        hideTimer = setTimeout(() => {
            showBubble.value = false
        }, 4500);
    }, 1000);
})

onUnmounted(() => {
    clearTimeout(showTimer)
    clearTimeout(hideTimer)
})
</script>