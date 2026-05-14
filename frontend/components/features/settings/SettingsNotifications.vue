<template>
    <BaseCard>
        <div class="notifications">

            <h2>Notifications</h2>
            <p>Choose what activity you want to be alerted about.</p>

            <div v-for="item in items" :key="item.key" class="notifications__row">
                
                <div>
                    <p>{{ item.label }}</p>
                    <p class="notification__description">{{ item.description }}</p>
                </div>
                <input type="checkbox" v-model="item.enabled" />
            </div>
            
            <button 
            @click="emit('save', items.reduce((a, item) => ({ ...a, [item.key]: item.enabled }), {}))">
            Save Preferences
            </button>

        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue';
import BaseCard from '@/components/ui/BaseCard.vue';

const emit = defineEmits(['save']);

const items = [
    { key: 'event_rsvp',           label: 'Event RSVPs',          description: 'Someone joins or declines your event.',  enabled: true  },
    { key: 'friend_request',       label: 'Friend Requests',      description: 'A user sends you a friend request.',     enabled: true  },
    { key: 'marketplace_interest',           label: 'Marketplace Interests',          description: 'New items match your interests.',  enabled: true  },
    { key: 'vault_update',       label: 'Vault Updates',      description: 'A user updates their vault.',     enabled: true  },
    { key: 'community_event',           label: 'Community Events',          description: 'New events in your community.',  enabled: true  },
  
]
</script>

<style scoped>
    .notifications {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.notifications__description {
    font-size: 0.875rem;
    color: #666;
}


.notifications__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #eee;
}

</style>