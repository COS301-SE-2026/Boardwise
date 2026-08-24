<template>
    <v-container fluid class="fill-height" align-start>
        <SearchResults
            :query="query"
            :people="people"
            :rulebooks="rulebooks"
            :listings="listings"
            :communities="communities"
        />
    </v-container>
</template>

<script setup>
import SearchResults from '~/components/features/search/SearchResults.vue'

import { LibraryService } from '~/services/libraryService'
import { MarketplaceService } from '~/services/marketplaceService'
import { CommunityService } from '~/services/communityService'
// import { PersonService } from '~/services/PersonService'

const route = useRoute()
const query = computed(() => route.query.q ?? '')

const people = ref([])
const rulebooks = ref([])
const listings = ref([])
const communities = ref([])

// Mock people 
const mockPeople = [
    {id: 1, username: 'catan_carla', mutualLable: '12 mutual games', isFriend: true, avatarUrl: null },
    {id: 2, username: 'settlersfan88', mutualLable: '3 mutual games', isFriend: false, avatarUrl: null },
]

async function runSearch(q) {
    if(!q) {
        people.value = []
        rulebooks.value = []
        listings.value = []
        communities.value = []
        return
    }

    people.value = mockPeople

    const [rulebookResult, listingResult, communityResult] = await Promise.allSettled([
        LibraryService.fetchAllRulebooks({ title: q }),
        MarketplaceService.getListings({ search: q }),
        CommunityService.searchForCommunity(q),
    ])

    rulebooks.value = rulebookResult.status === 'fulfilled'
        ? (rulebookResult.value?.content ?? []).map(rb => ({
            id: rb.id,
            title: rb.title,
            coverUrl: rb.coverUrl,
            genre: rb.genres?.[0] ?? '',
        })) 
        : []
    
    listings.value = listingResult.status === 'fulfilled'
        ? (listingResult.value?.content ?? listingResult.value ?? []).map(l => ({
            id: l.listingId,
            title: l.listingTitle,
            price: l.price,
            imageUrl: l.imageUrl ?? null,
        }))
        : []
    
    communities.value = communityResult.status === 'fulfilled'
        ? communityResult.value.map(c => ({
            id: c.id,
            name: c.name,
            description: c.description,
            imageUrl: c.imageUrl,
            visibility: c.visibility,
            memberCount: c.memberCount,
        }))
        : []
}

watch(query, runSearch, { immediate: true })

</script>