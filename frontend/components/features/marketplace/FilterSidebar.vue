<template>
    <div class="sidebar">

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
            <label><input type="checkbox" v-model="filters.rent" /> Rent</label>
            <label><input type="checkbox" v-model="filters.sale" /> For Sale</label>
        </BaseFilterGroup>

        <BaseFilterGroup title="Price Range">
            <div class="price-row">
                <div class="price-input">
                    <span>R</span>
                    <input v-model="filters.minPrice" placeholder="Min" type="number" />
                </div>

                <div class="price-input">
                    <span>R</span>
                    <input v-model="filters.maxPrice" placeholder="Max" type="number" />
                </div>
            </div>
        </BaseFilterGroup>

        <BaseFilterGroup title="Condition">
            <label v-for="c in conditions" :key="c">
                <input type="checkbox" /> {{ c }}
            </label>
        </BaseFilterGroup>

        <button class="reset-btn" @click="resetFilters">↺ Reset Filters</button>
    </div>
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
.sidebar {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    min-width: 220px;
    width: 220px;
}

.category-option {
  font-size: 13px;
  color: #555;
  cursor: pointer;
  padding: 2px 0;
}

.category-option:hover,
.category-option.active {
  color: #6C3BFF;
  font-weight: 600;
}

label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
}

input[type="checkbox"] {
  accent-color: #6C3BFF;
}

.price-row {
  display: flex;
  gap: 8px;
}

.price-input {
  display: flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 6px 10px;
  flex: 1;
  font-size: 13px;
}

.price-input input {
  border: none;
  outline: none;
  width: 100%;
  font-size: 13px;
}

.reset-btn {
  margin-top: 12px;
  background: none;
  border: none;
  color: #6C3BFF;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  text-align: left;
  padding: 0;
}
</style>