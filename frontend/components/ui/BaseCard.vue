// Base Card for every other Card (Marketplace, Library and Profile Cards)
<template> 
    <div class="card">
        <slot />
    </div>
</template>

<style scoped> 
.card {
    background: white;
    border-radius: 16px; 
    padding: 16px; 
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
</style>