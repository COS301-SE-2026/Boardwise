<template>
    <PageContainer>
        <Navbar/>

        <ChatLayout/>
    </PageContainer>
</template>

<script setup>
import ChatLayout from '~/components/features/chat/ChatLayout.vue';
import Navbar from '~/components/layout/Navbar.vue';
import PageContainer from '~/components/layout/PageContainer.vue';

</script>