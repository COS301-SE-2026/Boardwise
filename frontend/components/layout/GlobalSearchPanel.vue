<template>
    <div class="global-search__panel" aria-live="polite">
        <p v-if="term.length < 2" class="global-search__status">Type at least 2 characters.</p>
        <p v-else-if="searching" class="global-search__status">Searching...</p>
        <p v-else-if="!hasResults" class="global-search__status">No matches for “{{ term }}”. Try another spelling.</p>

        <template v-else>
        <section v-if="rulebooks.length">
            <h3 class="global-search__heading">Rulebooks</h3>
            <button v-for="r in rulebooks.slice(0, 3)" :key="r.id" class="global-search__item" @click="$emit('go', `/library/${r.id}`)">
            <BaseImage :src="r.coverUrl ?? ''" alt="" height="40px" width="40px" rounded="md" />
            <span class="global-search__text">
                <span class="global-search__title">{{ r.title }}</span>
                <span class="global-search__meta">{{ r.genre }}</span>
            </span>
            </button>
        </section>

        <section v-if="listings.length">
            <h3 class="global-search__heading">Marketplace</h3>
            <button v-for="l in listings.slice(0, 3)" :key="l.id" class="global-search__item" @click="$emit('go', `/marketplace/${l.id}`)">
            <BaseImage :src="l.imageUrl ?? ''" alt="" height="40px" width="40px" rounded="md" />
            <span class="global-search__text">
                <span class="global-search__title">{{ l.listingTitle }}</span>
                <span class="global-search__meta">R{{ l.price }} · {{ l.username }}</span>
            </span>
            </button>
        </section>

        <section v-if="people.length">
            <h3 class="global-search__heading">People</h3>
            <button v-for="p in people.slice(0, 3)" :key="p.id" class="global-search__item" @click="$emit('go', `/profile/${p.id}`)">
            <BaseAvatar :src="p.avatarUrl ?? ''" :name="p.username" size="sm" />
            <span class="global-search__text">
                <span class="global-search__title">@{{ p.username }}</span>
                <span v-if="p.isFriend" class="global-search__meta">Friend</span>
            </span>
            </button>
        </section>

            <section v-if="communities.length">
                <h3 class="global-search__heading">Communities</h3>
                <button v-for="c in communities.slice(0, 3)" :key="c.id" class="global-search__item" @click="$emit('go', `/social/community/${c.id}`)">
                <BaseImage :src="c.imageUrl ?? ''" alt="" height="40px" width="40px" rounded="md" />
                <span class="global-search__text">
                    <span class="global-search__title">{{ c.name }}</span>
                    <span class="global-search__meta">{{ c.memberCount }} members</span>
                </span>
                </button>
            </section>

        <button class="global-search__all" @click="$emit('all')">See all results for “{{ term }}”</button>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import BaseImage from '../ui/BaseImage.vue'
import BaseAvatar from '../ui/BaseAvatar.vue'

const props = defineProps({
  term: { type: String, default: '' },
  searching: { type: Boolean, default: false },
  people: { type: Array, default: () => [] },
  rulebooks: { type: Array, default: () => [] },
  listings: { type: Array, default: () => [] },
  communities: { type: Array, default: () => [] }
})
defineEmits(['go', 'all'])

const hasResults = computed(() =>
  props.people.length + props.rulebooks.length + props.listings.length + props.communities.length > 0
)
</script>