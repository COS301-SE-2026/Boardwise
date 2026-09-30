<template> 
    <div ref="rootRef" class="global-search" @focusout="onFocusOut">
        <BaseSearch 
            v-model="query"
            :placeholder="props.placeholder"
            aria-label="Search Boardwise"
            role="combobox"
            aria-autocomplete="list"
            aria-haspopup="listbox"
            :aria-expanded="String(open)"
            :aria-controls="listboxId"
            :aria-activedescendant="activeId"
            @focus="focused = true"
            @keydown="onKeydown"
        />

        <Teleport to="body">
            <div v-if="open" class="global-search__panel" :style="panelStyle" @mousedown.prevent>
                <div v-if="showInitialLoading" class="global-search__status">
                    <BaseSpinner size="xs" />
                    <span>Searching...</span>
                </div>

                <div v-else-if="showEmpty" class="global-search__status">
                    <v-icon size="20" aria-hidden="true">mdi-magnify-close</v-icon>
                    <span>No matches for "{{ trimmed }}"</span>
                </div>

                <div v-else :id="listboxId" role="listbox" aria-label="Search results" class="global-search__list" :class="{ 'global-search__list--stale': loading }"> <!-- //NOSONAR -->
                    <template v-for="section in sections" :key="section.key">
                        <div class="global-search__group">
                            {{ section.label }}
                        </div>

                        
                        <div v-for="item in section.items" :id="item.domId" :key="item.domId" role="option" class="global-search__option" :class="{ 'global-search__option--active': item.index === activeIndex }" :aria-selected="String(item.index === activeIndex)" @mouseenter="activeIndex = item.index" @click="select(item)"> <!-- //NOSONAR -->
                            <BaseAvatar 
                                v-if="section.key === 'people'"
                                :src="item.image"
                                :name="item.title"
                                size="sm"
                                class="global-search__thumb"
                            />

                            <span v-else class="global-search__thumb global-search__tile" aria-hidden="true">
                                <BaseImage
                                    v-if="item.image"
                                    :src="item.image"
                                    alt=""
                                    height="32px"
                                    width="32px"
                                />

                                <v-icon v-else size="20">{{ section.icon }}</v-icon>
                            </span>

                            <span class="global-search__text">
                                <span class="global-search__title">{{ item.title }}</span>
                                <span v-if="item.subtitle" class="global-search__subtitle">{{ item.subtitle }}</span>
                            </span>
                        </div>
                    </template>
                </div>

                <output class="sr-only" aria-live="polite">{{ announcement }}</output>
            </div>
        </Teleport>
    </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, useId } from 'vue'
import { useRouter } from 'vue-router'
import { useDebounceFn, useElementBounding } from '@vueuse/core'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseSpinner from '~/components/ui/BaseSpinner.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

import { useSearch } from '~/composables/useSearch'

const props = defineProps({
    placeholder: { type: String, default: 'Search people, rulebooks, listings, communities, events'}
})

const emit = defineEmits(['select'])

const router = useRouter()
const { people, rulebooks, listings, communities, events, loading, search } = useSearch()

const MIN_CHARS = 2
const PER_GROUP = 4
const DEBOUNCE_MS = 300

const rootRef = ref(null)
const query = ref('')
const focused = ref(false)
const activeIndex = ref(-1)

const listboxId = `global-search-${useId()}`
const trimmed = computed(() => query.value.trim())

const open = computed(() => focused.value && trimmed.value.length >= MIN_CHARS)

// Search 
const runSearch = useDebounceFn((q) => search(q), DEBOUNCE_MS)

watch(trimmed, (q) => {
    activeIndex.value = -1
    if (q.length >= MIN_CHARS) {
        focused.value = true
        runSearch(q)
    }
})

const formatDate = (iso) => {
    const d = new Date(iso)
    return Number.isNaN(d) ? '' : d.toLocaleDateString('en-ZA', { day: 'numeric', month: 'short' })
}

// Result of search ==> Mapping to row display + destination of each [id].vue
const mappers = {
    people: (p) => ({
        id: p.id,
        title: p.displayName || p.name || p.username,
        subtitle: p.username ? `@${p.username}` : '',
        image: p.avatarUrl || p.avatar,
        path: `/profile/${p.id}`
    }),
    rulebooks: (r) => ({
        id: r.id,
        title: r.title || r.name,
        subtitle: r.genre || '',
        image: r.coverUrl,
        path: `/library/${r.id}`
    }), 
    listings: (l) => ({
        id: l.id,
        title: l.listingTitle || l.gameTitle,
        subtitle: [l.gameTitle, l.price != null ? `R${l.price}` : ''].filter(Boolean).join(' · '),
        image: l.imageUrl,
        path: `/marketplace/${l.id}`
    }),
    communities: (c) => ({
        id: c.id,
        title: c.name || c.title,
        subtitle: c.memberCount != null ? `${c.memberCount} members` : '',
        image: c.imageUrl || c.image,
        path: `/social/community/${c.id}`
    }),
    events: (e) => ({
        id: e.id,
        title: e.name, 
        subtitle: [formatDate(e.startTime), e.location].filter(Boolean).join(' · '),
        image: e.imageUrl,
        path: `/events/${e.id}`
    })
}

const groups = [
    {key: 'people', label: 'Friends', icon: 'mdi-account', source: people },
    {key: 'rulebooks', label: 'Rulebooks', icon: 'mdi-book-open-page-variant', source: rulebooks },
    {key: 'listings', label: 'Listings', icon: 'mdi-storefront-outline', source: listings },
    {key: 'communities', label: 'Communities', icon: 'mdi-account-group', source: communities },
    {key: 'events', label: 'Events', icon: 'mdi-calendar', source: events },
]

// Sections
const sections = computed(() => {
    let index = 0
    return groups.map((g) => ({ 
        ...g, 
        items: (g.source.value ?? []).slice(0, PER_GROUP).map((raw) => ({
            ...mappers[g.key](raw),
            domId: `${listboxId}-${g.key}-${raw.id}`,
            index: index++
        }))
    }))

    .filter((s) => s.items.length)
})

const flatItems = computed(() => sections.value.flatMap((s) => s.items))

const showInitialLoading = computed(() => loading.value && !flatItems.value.length)
const showEmpty = computed(() => !loading.value && !flatItems.value.length)

const activeId = computed(() => 
    open.value && activeIndex.value >= 0 ? flatItems.value[activeIndex.value]?.domId : undefined
)

const announcement = computed(() => {
    if(loading.value) return 'Searching'
    const n = flatItems.value.length
    if(n === 0) return 'No results'
    const noun = n === 1 ? 'result' : 'results'
    return `${n} ${noun} available`
})

const { left, bottom, width, update } = useElementBounding(rootRef)

const panelStyle = computed(() => {
    const vw = typeof window !== 'undefined' ? window.innerWidth : 0
    const w = Math.min(Math.max(width.value, 320), vw - 16)
    const l = Math.min(Math.max(left.value, 8), vw - w - 8)
    return { top: `${bottom.value + 8}px`, left: `${l}px`, width: `${w}px` }
})

watch(open, (isOpen) => {
    if (isOpen) nextTick(update)
})

// Interaction
function select(item) {
    router.push(item.path)
    emit('select')
    query.value = ''
    focused.value = false
    activeIndex.value = -1
    document.activeElement?.blur?.()
}

function move(delta) {
    const n = flatItems.value.length
    if(!n) return
    activeIndex.value = (activeIndex.value + delta + n) % n
}

function onKeydown(e) {
    switch (e.key) {
        case 'ArrowDown' :
            e.preventDefault()
            focused.value = true
            move(1)
            break
        
        case 'ArrowUp' :
            e.preventDefault()
            if (activeIndex.value === -1) activeIndex.value = flatItems.value.length
            move(-1)
            break

        case 'Enter': {
            if(!open.value || !flatItems.value.length) return
            e.preventDefault()
            select(flatItems.value[Math.max(activeIndex.value, 0)])
            break
        }

        case 'Escape': 
            if(open.value){
                e.preventDefault()
                focused.value = false
            } else {
                query.value = ''
            }
            break
    }
}

function onFocusOut(e) {
    if(!rootRef.value?.contains(e.relatedTarget)) focused.value = false
}

// Keep highlighted row visible in the scrolling list
watch (activeIndex, async () => {
    await nextTick()
    if (activeId.value) document.getElementById(activeId.value)?.scrollIntoView({ block: 'nearest'})
})
</script>

<style scoped>
.global-search {
  position: relative;
  width: 100%;
}

/* Input */
.global-search .base-search .v-field {
  min-height: 48px;
  border-radius: var(--radius-pill);
}

/* Results panel (teleported to body) */
.global-search__panel {
  position: fixed;
  z-index: 2500;
  max-height: min(480px, 70vh);
  overflow-y: auto;
  padding: var(--space-2);
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.global-search__status {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  color: var(--color-text-muted);
}

.global-search__list--stale {
  opacity: 0.6;
  transition: opacity var(--transition-fast);
}

.global-search__group {
  padding: var(--space-3) var(--space-3) var(--space-1);
  font-family: var(--font-body);
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.global-search__option {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-height: 56px;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--color-text);
}
.global-search__option--active { background: var(--color-surface-alt); }

.global-search__thumb { flex: none; }

.global-search__tile {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  overflow: hidden;
  border-radius: var(--radius-sm);
  background: var(--color-surface-alt);
  color: var(--color-text-muted);
}

.global-search__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.global-search__title,
.global-search__subtitle {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.global-search__title { font-weight: var(--fw-bold); }
.global-search__subtitle {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

/* Desktop: bigger bar, bigger results */
@media (min-width: 1280px) {
  .global-search .base-search .v-field {
    min-height: 54px;
    font-size: 1.05rem;
  }

  .global-search__panel { max-height: min(640px, 70vh); }

  .global-search__group { font-size: 0.8rem; }

  .global-search__option {
    min-height: 68px;
    gap: var(--space-4);
  }

  .global-search__tile,
  .global-search__thumb {
    width: 48px;
    height: 48px;
  }

  .global-search__title {
    font-size: 1.05rem;
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .global-search__subtitle { font-size: 0.95rem; }
}

@media (max-width: 600px) {
  .global-search__panel { max-height: 60vh; }
}
</style>