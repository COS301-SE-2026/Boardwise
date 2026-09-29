<template>
    <div class="global-search">
        <BaseButton v-if="compact && !open" icon variant="text" aria-label="Search" @click="openSearch">
            <v-icon size="26">mdi-magnify</v-icon>
        </BaseButton>

        <Teleport v-else to="body" :disabled="!compact">
            <div ref="layer" class="global-search__layer" :class="{ 'global-search__layer--overlay': compact }">
                <div class="global-search__bar">
                    <BaseButton v-if="compact" icon variant="text" aria-label="Close search" @click="close">
                        <v-icon>mdi-arrow-left</v-icon>
                    </BaseButton>

                    <BaseSearch
                        v-model="q"
                        placeholder="Search games, people, rulebooks..."
                        aria-label="Search Boardwise"
                        :autofocus="compact"
                        @focus="open = true"
                        @keyup.enter="goToAll"
                        @keyup.esc="close"
                    />
                </div>

                <div v-if="showPanel" class="global-search__panel" aria-live="polite">
                    <p v-if="term.length < 2" class="global-search__status">Type at least 2 characters.</p>
                    <p v-else-if="!settled" class="global-search__status">Searching...</p>
                    <p v-else-if="!hasResults" class="global-search__status">No matches for “{{ term }}”. Try another spelling.</p>

                    <template v-else>
                        <section v-if="rulebooks.length">
                            <h3 class="global-search__heading">Rulebooks</h3>
                            <button v-for="r in rulebooks.slice(0, 3)" :key="r.id" class="global-search__item" @click="go(`/library/${r.id}`)">
                                <BaseImage :src="r.coverUrl" alt="" height="40px" width="40px" rounded="md" />
                                <span class="global-search__text">
                                    <span class="global-search__title">{{ r.title }}</span>
                                    <span class="global-search__meta">{{ r.genre }}</span>
                                </span>
                            </button>
                        </section>

                        <section v-if="listings.length">
                            <h3 class="global-search__heading">Marketplace</h3>
                            <button v-for="l in listings.slice(0, 3)" :key="l.id" class="global-search__item" @click="go(`/marketplace/${r.id}`)">
                                <BaseImage :src="l.imageUrl" alt="" height="40px" width="40px" rounded="md" />
                                <span class="global-search__text">
                                    <span class="global-search__title">{{ l.listingTitle }}</span>
                                    <span class="global-search__meta">R{{ l.price }} {{ l.username }}</span>
                                </span>
                            </button>
                        </section>

                        <section v-if="people.length">
                            <h3 class="global-search__heading">People</h3>
                            <button v-for="p in people.slice(0, 3)" :key="p.id" class="global-search__item" @click="go(`/profiLe/${p.id}`)">
                                <BaseAvatar :src="p.avatarUrl" alt="" height="40px" width="40px" size="sm" />
                                <span class="global-search__text">
                                    <span class="global-search__title">{{ p.username }}</span>
                                    <span v-if="p.isFriend" class="global-search__meta">Friend</span>
                                </span>
                            </button>
                        </section>

                        <section v-if="communities.length">
                            <h3 class="global-search__heading">Communities</h3>
                            <button v-for="c in communities.slice(0, 3)" :key="c.id" class="global-search__item" @click="go(`/social/community/${c.id}`)">
                                <BaseImage :src="c.imageUrl" alt="" height="40px" width="40px" rounded="md" />
                                <span class="global-search__text">
                                    <span class="global-search__title">{{ c.name }}</span>
                                    <span class="global-search__meta">{{ c.memberCount }} members</span>
                                </span>
                            </button>
                        </section>

                        <button class="global-search__all" @click="goToAll">See all results for "{{  term  }}"</button>
                    </template>
                </div>
            </div>
        </Teleport>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useDebounceFn, onClickOutside } from '@vueuse/core'
import { useSearch } from '~/composables/useSearch'

import BaseButton from '../ui/BaseButton.vue'
import BaseSearch from '../ui/BaseSearch.vue'
import BaseImage from '../ui/BaseImage.vue'
import BaseAvatar from '../ui/BaseAvatar.vue'

const router = useRouter()
const route = useRoute()
const { lgAndUp } = useDisplay()
const compact = computed(() => !lgAndUp.value)

const { people, rulebooks, listings, communities, search } = useSearch()

const q = ref('')
const open = ref(false)
const settled = ref(false)
const layer = ref(null)

const term = computed(() => q.value.trim())
const hasResults = computed(() => people.value.length + rulebooks.value.length + listings.value.length + communities.value.length > 0)
const showPanel = computed(() => compact.value || (open.value && term.value.length >= 1))

const run = useDebounceFn(async (v) => {
  if (v.length < 2) { await search(''); return }
  await search(v)
  settled.value = true
}, 300)

watch(q, (v) => { settled.value = false; run(v.trim()) })

const openSearch = () => { open.value = true }
const close = () => { open.value = false; q.value = '' }
const go = (path) => { router.push(path) }
const goToAll = () => { if (term.value) router.push({ path: '/search', query: { q: term.value } }) }

watch(() => route.fullPath, close)
onClickOutside(layer, () => { if (!compact.value) open.value = false })
</script>