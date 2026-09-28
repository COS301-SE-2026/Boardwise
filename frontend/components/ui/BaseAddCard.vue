<!-- Profile AddGame Card and AddListing Card -->
 <template>
    <BaseCard clickable class="base-add-card" @click="$emit('click')">
        <div class="add-card__content">
            <span class="add-card__plus"><v-icon>mdi-plus</v-icon></span>
            <p class="card-meta">{{ label }}</p>
        </div>
    </BaseCard>
</template>

<script setup>
import BaseCard from './BaseCard.vue';

defineProps({ label: {type: String, default:'Add' } })
defineEmits(['click'])
</script>