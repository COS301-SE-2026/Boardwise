<template> 
    <button @click="$emit('click')>
        <slot />
    </button>
</template>

<style scoped>
.btn {
  padding: 8px 12px;
  cursor: pointer;
}
</style>