<template>
    <BaseCard>
        <div class="privacy">
            <h2>Privacy</h2>
            <p>Control who can see your profile and activity.</p>

            <div class="privacy__field">
                <label>Profile Visibility</label>
                <select v-model="visibility">
                    <option value="public">Public view</option>
                    <option value="community">Community members only</option>
                    <option value="private">Private</option>
                </select>
            </div>

            <div v-for="item in items" :key="item.key" class="privacy__row">
                <div>
                    <p>{{ item.label }}</p>
                    <p class="privacy__description">{{ item.description }}</p>
                </div>
                <input type="checkbox" v-model="item.enabled" />
            </div>

            <button @click="emit('save', { visibility, items: items.reduce((a, item) => ({ ...a, [item.key]: item.enabled }), {}) })">
                Save Privacy Settings</button>
            
           
        </div>
    </BaseCard>
</template>

<script setup>
import { ref } from 'vue';
import BaseCard from '@/components/ui/BaseCard.vue';

const items = [
    {key: 'show_online_status', label: 'Show Online Status', description: 'Allow others to see when you are online.', enabled: true},
    {key: 'show_activity', label: 'Show Activity', description: 'Allow others to see your recent activity.', enabled: true},
    {key: 'show_friends', label: 'Show Friends List', description: 'Allow others to see your friends list.', enabled: true},
    {key: 'show_events', label: 'Show Events', description: 'Allow others to see the events you are attending.', enabled: true},
    {key: 'show_marketplace', label: 'Show Marketplace', description: 'Allow others to see your marketplace activity.', enabled: true}
]
</script>

<style scoped>
    .privacy {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.privacy__field {
    display: flex;
    flex-direction: column;
    gap: 6px;
}



.privacy__row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #eee;
}

.privacy__description {
    font-size: 0.875rem;
    color: #666;

}

</style>