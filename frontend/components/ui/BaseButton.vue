<template> 
    <button class="base-button">
        <slot />
    </button> 
</template>

<style scoped>
.base-button {
        padding: 10px 16px;
        border: none;
        border-radius: 8px; 
        background: #7B2CBF;
        color: white;
        cursor: pointer;
}
</style>