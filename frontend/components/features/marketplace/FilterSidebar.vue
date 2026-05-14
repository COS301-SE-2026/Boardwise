<template>
    <div class="sidebar">

        <h3>Filters</h3>

        <label> 
            <input type="checkbox" />
            Buy 
        </label>

        <label> 
            <input type="checkbox" />
            Rent 
        </label>
    </div>
</template>

<style scoped>
.sidebar {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    min-width: 220px;
}

label {
    display: flex;
    gap: 8px;
}
</style>