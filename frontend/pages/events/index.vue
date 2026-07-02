<template>
  <PageContainer>
    <Navbar />
    <SectionTitle title="Events" subtitle="Discover and join gaming events near you" />
    <p class="text-medium-emphasis mt-4 mb-8">
      Find local gaming events, tournaments, and meetups.
      Connect with fellow board game enthusiasts.
    </p>
    <v-empty-state
      title="Page Under Construction"
      text="Events are coming soon. Check back later!"
      icon="mdi-hammer-wrench"
    />
  </PageContainer>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import { onMounted } from 'vue';
import { useRouter } from 'vue-router'

const router = useRouter()
onMounted(() => {
  if (!localStorage.getItem('access_token')) {
    router.push('/auth/signin')
  }
})
</script>