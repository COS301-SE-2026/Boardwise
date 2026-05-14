<template> 
    <div class="recommended">

        <SectionTitle 
            title="Recommnded Books"
            subtitle="More rulebooks you may enjoy"
        />

        <RulebookGrid />
    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'

import RulebookGrid from './RulebookGrid.vue'
</script>

<style scoped> 
.recommended {
    display: flex;
    flex-direction: column;

    gap: 24px;
    margin-top: 40px;
}
</style>