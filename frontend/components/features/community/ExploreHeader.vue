<template>
   <PageHeader
        data-test="explore-header"
        title="Social"
        subtitle="Find your people"
        action-label="Create Community"
        action-icon="mdi-plus"
        @action="$emit('create-community')"
   >
        <template #search>
            <ExploreSearch
                data-test="explore-search"
                @search="$emit('search', $event)"
            />
        </template>
   </PageHeader>
</template>

<script setup>
import PageHeader from '~/components/layout/PageHeader.vue'
import ExploreSearch from './ExploreSearch.vue'

const props = defineProps({
    searchQuery: { type: String, default: ''}
})

const emit = defineEmits(['search', 'create-community'])
</script>