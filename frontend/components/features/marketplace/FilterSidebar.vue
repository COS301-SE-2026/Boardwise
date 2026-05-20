<template>
    <v-sheet class="pa-4 rounded-lg" width="220" min-width="220">

        <h3>Filters</h3>

        <BaseFilterGroup title="Categories">
            <div 
                v-for="category in categories" 
                :key="category"
                class="category-option"
                :class="{ active: selectedCategory === category }"
                @click="selectedCategory = category"
            >
                {{  category }}
            </div>
        </BaseFilterGroup>

        <BaseFilterGroup title="Listing Type">
            <v-checkbox v-model="filters.rent" label="Rent" density="compact" color="primary" hide-details />
            <v-checkbox v-model="filters.sale" label="For Sale" density="compact" color="primary" hide-details />        </BaseFilterGroup>

        <BaseFilterGroup title="Price Range">
            <div class="d-flex ga-2">
                <v-text-field v-model="filters.minPrice" placeholder="Min" prefix="R" type="number" density="compact" variant="outlined" hide-details />
                <v-text-field v-model="filters.maxPrice" placeholder="Max" prefix="R" type="number" density="compact" variant="outlined" hide-details />
            </div>
        </BaseFilterGroup>

        <BaseFilterGroup title="Condition">
            <v-checkbox v-for="c in conditions" 
                :key="c" 
                :label="c" 
                density="compact" 
                color="primary" 
                hide-details />

        </BaseFilterGroup>

        <v-btn variant="text" color="primary" class="mt-4 pa-0" @click="resetFilters">↺ Reset Filters</v-btn>
    </v-sheet>
</template>

<script setup>
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue';

const categories = ['All Categories', 'Strategy', 'Family', 'Party', 'Card', 'Abstract'];
const conditions = ['New', 'Like New', 'Good', 'Fair'];
const selectedCategory = ref('All Categories');

const filters = reactive({
    rent: false,
    sale: false,
    minPrice: '',
    maxPrice: ''
});

const resetFilters = () => {
    filters.rent = false;
    filters.sale = false;
    filters.minPrice = '';
    filters.maxPrice = '';
    selectedCategory.value = 'All Categories';
}

</script>

<style scoped>


.category-option {
  font-size: 13px;
  color: #555;
  cursor: pointer;
  padding: 2px 0;
}

.category-option:hover,
.category-option.active {
    color: rgb(var(--v-theme-primary));
    font-weight: 600;
}



</style>