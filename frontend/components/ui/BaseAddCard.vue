<!-- Profile AddGame Card and AddListing Card -->
 <template>
    <BaseCard clickable class="base-add-card" @click="$emit('click')">
        <span class="add-card__plus"><v-icon>mdi-plus</v-icon></span>
        <p class="card-meta">{{ label }}</p>
    </BaseCard>
 </template>

 <script setup>
defineProps({ label: {type: String, default:'Add' } })
defineEmits(['click'])
</script>