<template>
  <div v-if="rulebook" class="rulebook">

    <SectionTitle :title="`${rulebook.title} — Rulebook`" />

    <BaseTabs
      :tabs="tabs"
      :active-tab="activeTab"
      @change="activeTab = $event"
    />

    <BaseCard class="rulebook__content">
      <p>{{ currentPage.content }}</p>
    </BaseCard>

    <div class="rulebook__footer">
    <BaseButton @click="router.push(`/library/${rulebook.id}`)">
        View full rulebook
    </BaseButton>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter

 } from 'vue-router'
import BaseTabs from '~/components/ui/BaseTabs.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const router = useRouter()

const props = defineProps({
  rulebook: { type: Object, default: null }
})

const tabs = computed(() => props.rulebook?.pages.map(p => p.title) ?? [])
const activeTab = ref(tabs.value[0])

const currentPage = computed(() =>
  props.rulebook?.pages.find(p => p.title === activeTab.value) ?? {}
)
</script>

<style scoped>
.rulebook {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.rulebook__content {
  padding: var(--space-6);
  line-height: var(--lh-relaxed);
  color: var(--color-text);
  font-size: var(--fs-body);
  min-height: 200px;
}
</style>