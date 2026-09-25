<template>
    <BaseFilterSidebar data-test="friends-filter-sidebar" @reset="resetFilters">
        <BaseFilterGroup title="Friend Status">
            <BaseFilterCheckboxGroup
                v-model="selectedStatuses"
                :options="[
                    {label: 'Request friend', value: 'none'},
                    {label: 'Request sent', value: 'requested'}
                ]"
            />
        </BaseFilterGroup>
    </BaseFilterSidebar>    
</template>

<script setup>
import { ref, watch } from 'vue'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue'

const emit = defineEmits(['filter'])

const selectedStatuses = ref([])

watch(selectedStatuses, () => {
    emit('filter', {
        statuses: selectedStatuses.value.length > 0 ? selectedStatuses.value : null
    })
}, {deep: true})

const resetFilters = () => {
    selectedStatuses.value = []
}
</script>