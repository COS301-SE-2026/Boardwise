<template> 
    <div class="recommended">

        <SectionTitle 
            title="Recommended For You"
        />

        <div class="rulebooks-row">
            <RulebookCard   
                v-for="rulebook in rulebooks"
                :key="rulebook.id"
                :rulebook="rulebook"
            />
        </div>
    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'
import RulebookCard from './RulebookCard.vue'

defineProps({
    rulebooks: {
        type: Array,
        default: () => []
    }
})
</script>

<style scoped> 
.recommended {
  margin-top: 40px;
}

.rulebooks-row {
    display: flex;
    gap: 16px;
    overflow-x: auto;
    padding-bottom: 8px;
}

/* Hide scrollbar */
.rulebooks-row::webkit-scrollbar {
    display: none;
}
</style>