<template>
  <PageContainer>
    <Navbar/>

    <ContentSection>
      <PageHeader
        title="Settings"
        subtitle="Manage your account preferences"
      />
      <SettingsView/>
    </ContentSection>
  </PageContainer>
</template>

<script setup>
import SettingsView from '@/components/features/settings/SettingsView.vue';
import ContentSection from '~/components/layout/ContentSection.vue';
import Navbar from '~/components/layout/Navbar.vue';
import PageContainer from '~/components/layout/PageContainer.vue';
import PageHeader from '~/components/layout/PageHeader.vue';

definePageMeta({
  middleware: 'auth'
})
</script>