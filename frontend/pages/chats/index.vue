<template>
    <PageContainer>
        <Navbar/>

        <ChatLayout
            :token="token"
        />
    </PageContainer>
</template>

<script setup>
import ChatLayout from '~/components/features/chat/ChatLayout.vue';
import Navbar from '~/components/layout/Navbar.vue';
import PageContainer from '~/components/layout/PageContainer.vue';

const router = useRouter()
const token = ref("")

onMounted(() => {
    if(!localStorage.getItem("access_token"))
        router.push("/auth/signin")
    else{
        token.value = localStorage.getItem("access_token")
    }
})

</script>