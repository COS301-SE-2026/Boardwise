<template>
   <PageHeader
        data-test="explore-header"
        title="Social"
        subtitle="Find your people"
        action-label="Create a community"
        action-icon = "mdi-plus"
        @action="$emit('create-community')"
   >
        <template #search>
            <ExploreSearch
                :model-value="searchQuery"
                @update:model-value="$emit('update:searchQuery', $event)"
            />
        </template>
   </PageHeader>
</template>

<script setup>
import PageHeader from '~/components/layout/PageHeader.vue'
import ExploreSearch from './ExploreSearch.vue'

defineProps({
    searchQuery: { type: String, default: ''}
})

defineEmits(['search', 'create-community'])
</script>