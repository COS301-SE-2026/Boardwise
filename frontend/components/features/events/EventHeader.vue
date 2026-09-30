<template>
    <PageHeader 
        data-test="events-header"
        title="Events"
        subtitle="Discover and join gaming events near you"
        action-label="Create Event"
        action-icon="mdi-calendar-plus"
        @action="$emit('create-event')"
    >
        <template #search>
            <EventSearch
                data-test="event-search"
                @search="$emit('search', $event)"
            />
        </template>
    </PageHeader>
</template>

<script setup>
import PageHeader from '~/components/layout/PageHeader.vue';
import EventSearch from './EventSearch.vue';

defineEmits(['search', 'create-event'])
</script>