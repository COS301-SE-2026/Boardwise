<template>
    <v-hover v-slot="{ isHovering }">
    <BaseCard class="pa-6 h-100" >
        <v-icon
            :icon="feature.icon"
            :size="isHovering ? 74 : 64"
            color="primary"
            class="mb-6"
        />
        <h3 class="text-h6 font-weight-bold">
            {{ feature.title }}
        </h3>

        <p class="text-body-2 text-medium-emphasis mt-2">
            {{ feature.description }}
        </p>

        <v-list>
            <v-list-item 
                v-for="highlight in feature.highlights"
                :key="highlight"
                prepend-icon="mdi-check-circle"
            >
                <v-list-item-title>
                    {{ highlight }}
                </v-list-item-title>
            </v-list-item>
        </v-list>
        <v-spacer/>
        
        <div class="d-flex align-center font-weight-medium cursor-pointer"
            @click="router.push(feature.route)"
            >
            
            Explore
            <v-icon 
                class="ms-2"
             >
                mdi-arrow-right
            </v-icon>
        </div>
    </BaseCard>
</v-hover>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

const router = useRouter()

defineProps({
    feature: {
        type: Object,
        required: true
    }
})
</script>
