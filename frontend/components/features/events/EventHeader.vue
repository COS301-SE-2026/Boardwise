<template>
    <div class="d-flex flex-column ga-5">

        <SectionTitle 
            title="Events"
            subtitle="Discover and join gaming events near you"
        />

        <EventSearch 
            @search="$emit('search', $event)"
            @create-event="$emit('create-event')"
        />
    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue';
import EventSearch from './EventSearch.vue';

defineEmits(['search', 'create-event'])
</script>