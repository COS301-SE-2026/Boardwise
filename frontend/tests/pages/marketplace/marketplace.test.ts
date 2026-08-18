import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

import MarketplacePage from '~/pages/marketplace/index.vue'
import type { VProgressCircular } from 'vuetify/components'

const fetchListings = vi.fn()
const addListing = vi.fn()
const loadMore = vi.fn()
const fetchRetail = vi.fn()

const listings = [
    {
        listingId: 1,
        listingTitle: 'Catan for Sale',
        gameTitle: 'Catan',
        listingType: 'sale',
        price: 500
    }
]

const retailResults = [
    {
        retailerName: 'Takealot',
        retailTitle: 'Catan Board Game',
        price: 899,
        imageUrl: '/catan.png',
        url: 'https://example.com/catan'
    }
]

vi.mock('~/composable/useMarketplace', () => ({
    useMarketplace: () => ({
        listings, 
        loading: false,
        fetchListings,
        addListing,
        loadMore,
        hasMore: false
    })
}))

vi.mock('~/composable/useRetail', () => ({
    useRetail: () => ({
        retailResults, 
        retailLoading: false,
        retailError: null,
        fetchRetail
    })
}))

describe('Marketplace index.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()

        localStorage.setItem('access_token', 'test-token')
    })

    afterEach(() => {
        localStorage.clear()
    })

    const mountPage = () => {
        return mount(MarketplacePage, {
            global: {
                stubs: {
                    PageContainer: {
                        template: `
                            <div data-test="page-container">
                                <slot />
                            </div>
                        `
                    },

                    Navbar: {
                        template: `<nav data-test="navbar"></nav>`
                    },

                    MarketplaceHeader: {
                        emits: [
                            'update:modelValue',
                            'create-listing'
                        ],
                        template: `
                            <div data-test="marketplace-header">
                                <button data-test="search-button"
                                    @click="$emit('update:modelValue', 'Catan')
                                >
                                    Search 
                                </button>
                                <button data-test="create-listing-button"
                                    @click="$emit('create-listing')
                                >
                                    Create Listing 
                                </button>
                            </div>
                        `
                    }, 

                    MarketplaceTabs: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                            <div data-test="marketplace-tabs">
                                <button data-test="community-tab" @click="$emit('update:modelValue', 'Community Listings')>
                                    Community Listings
                                </button>

                                <button data-test="web-tab" @click="$emit('update:modelValue', 'Web')>
                                    Web
                                </button>
                            </div>
                        `
                    }, 
                    
                    FilterSidebar: {
                        template: `<div data-test="filter-sidebar"></div>`
                    },

                    ListingGrid: {
                        props: ['listings'],
                        template: `
                            <div data-test="listing-grid">
                                {{ listings.length }}
                            </div>
                        `
                    }, 

                    RetailerGrid: {
                        props: ['retailers'],
                        template: `
                            <div data-test="retailer-grid">
                                {{ retailers.length }}
                            </div>
                        `
                    }, 

                    AddLisitingModal: {
                        template: '<div data-test="add-listing-modal"></div>'
                    }, 

                    VNavigationDrawer: {
                        template: '<div><slot /></div>'
                    },

                    VChip: {
                        template: '<div><slot /></div>'
                    },

                    VContainer: {
                        template: '<div><slot /></div>'
                    },

                    VProgressCircular: {
                        template: '<div data-test="loading-spinner"></div>'
                    }
                }
            }
        })
    }

    it('renders the marketplace page', () => {
        const wrapper = mountPage()

        expect(wrapper.find('[data-test="page-container"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="navbar"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="marketplace-header"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="marketplace-tabs"]').exists()).toBe(true)
    })

    it('shows community listings by default', () => {
        const wrapper = mountPage()

        expect(wrapper.find('[data-test="filter-sidebar"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="listing-grid"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="retailer-grid"]').exists()).toBe(true)
    })

    it('fetches community listings when the page is mounted', () => {
        mountPage()

        expect(fetchListings).toHaveBeenCalledWith({}, true)
    })

    it('switches to the external retail section', async () => {
        const wrapper = mountPage()

        await wrapper.find('[data-test="web-tab"]').trigger('click')
        await nextTick()

        expect(wrapper.find('[data-test="listing-grid"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="retailer-grid"]').exists()).toBe(true)
        expect(fetchRetail).toHaveBeenCalled()
    })

    it('passes retail results to RetailerGrid', async () => {
        const wrapper = mountPage()

        await wrapper.find('[data-test="web-tab"]').trigger('click')
        await nextTick()

        expect(wrapper.find('[data-test="retailer-grid"]').text()).toContain(1)
    })

    it('updates retail search when searching on the Web tab', async () => {
        const wrapper = mountPage()

        await wrapper.find('[data-test="web-tab"]').trigger('click')
        await nextTick()

        expect(fetchRetail).toHaveBeenCalled()

        await wrapper.find('[data-test="search-button"]').trigger('click')
        await nextTick()
    })

    it('does not show the retailer grid while retail results are loading', async () => {
        const wrapper = mountPage()

        await wrapper.find('[data-test="web-tab"]').trigger('click')
        await nextTick()

        expect(wrapper.find('[data-test="retailer-grid"]').exists()).toBe(true)
    })
})