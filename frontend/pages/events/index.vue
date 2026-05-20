<template>
    <Navbar />
    <div class="events-page">
        <SectionTitle title="Events" subtitle="Discover and join gaming events near you"/>
        <p class="description">Find local gaming events, tournaments, and meetups. Connect with fellow board game enthusiasts and share your passion for gaming.</p>

        <h2>Page Underconstruction</h2>
    </div>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
</script>

<style scoped>
.events-page {
    padding: 40px;
}

.description {
    margin-top: 16px;
    color: #555;
    line-height: 1.6;
}

h2 {
    margin-top: 40px;
    color: #6C3BFF;
}

@media (max-width: 768px) {
    .events-page {
        padding: 20px;
    }

    h2 {
        font-size: 24px;
    }
}

</style>