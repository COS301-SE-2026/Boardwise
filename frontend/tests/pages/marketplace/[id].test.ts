import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

import ListingPage from '~/pages/marketplace/[id].vue'

const fetchListingById = vi.fn()

vi.mock('vue-router', () => ({
    useRoute: () => ({
        params: {
            id: '123'
        }
    })
}))

vi.mock('~/composable/useMarketplace', () => ({
    useMarketplace: () => ({
        fetchListingById, 
        loading: false
    })
}))


describe('marketplace/[id].vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()

        localStorage.setItem('access_token', 'test-token')
    })

    afterEach(() => {
        localStorage.clear()
    })

    const listing = {
        listingId: 123,
        gameTitle: 'Catan',
        listingType: 'sale',
        price: 500,
        username: 'hayley',
        location: 'Pretoria',
        genres: ['Strategy'],
        description: 'A great board game.'
    }


    const mountPage = () => {
        return mount(ListingPage, {
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

                    ListingDetail: {
                        props: ['listings'],
                        template: `
                            <div data-test="listing-detail">
                                {{ listings.gameTitle }}
                            </div>
                        `
                    }
                }
            }
        })
    }

    it('renders the listing detail page', async () => {
        fetchListingById.mockResolvedValue(listing)

        const wrapper = mountPage()
        await nextTick()

        expect(wrapper.find('[data-test="page-container"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="navbar"]').exists()).toBe(true)
    })

    it('fetches the listings detail page', async () => {
        fetchListingById.mockResolvedValue(listing)

        const wrapper = mountPage()
        await nextTick()

        expect(wrapper.find('[data-test="page-container"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="navbar"]').exists()).toBe(true)
    })

    it('fetches the listings using route id', async () => {
        fetchListingById.mockResolvedValue(listing)

        const wrapper = mountPage()
        await nextTick()

        expect(fetchListingById).toHaveBeenCalledWith('123')
    })

    it('renders ListingDetail when a listing is found', async () => {
        fetchListingById.mockResolvedValue(listing)

        const wrapper = mountPage()
        await nextTick()

        expect(wrapper.find('[data-test="listing-detail"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="listing-detail"]').text()).toContain('Catan')
    })

    it('shows listing not found when no listing returned', async () => {
        fetchListingById.mockResolvedValue(listing)

        const wrapper = mountPage()
        await nextTick()

        expect(wrapper.find('[data-test="listing-not-found"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="listing-not-found"]').text()).toBe('Listing not found.')
    })
})