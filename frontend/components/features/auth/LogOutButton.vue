<template> 
    <BaseButton variant="secondary" @click="handleLogout">
        <v-icon start>
            mdi-logout
        </v-icon>

        Logout
    </BaseButton>
</template>

<script setup>
import BaseButton from '../ui/BaseButton.vue'

const router = useRouter()
const { logout } = useAuth()

const handleLogout = async () => {
    await logout()
    router.push('/')
}
</script>