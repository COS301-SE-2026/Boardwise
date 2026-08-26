import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ListingDetail from '~/components/features/marketplace/ListingDetail.vue'
import { isEmpty } from 'vuetify/lib/util/helpers.mjs'

describe('ListingDetail.vue', () => {
    const listing: {
        listingId: number
        gameTitle: string
        listingType: string
        price: number
        username?: string
        location?: string
        imageUrl?: string
        genres?: string[]
        description?: string
        rentalPeriod?: {
            startDate: string
            endDate: string
        }
    } = {
        listingId: 1,
        gameTitle: 'Catan',
        listingType: 'sale',
        price: 500,
        username: 'hayley',
        location: 'Pretoria',
        imageUrl: '/catan.png',
        genres: ['Strategy', 'Family'],
        description: 'A lightly used copy of Catan.'
    }

    const mountListingDetail = (listingData: typeof listing = listing) => {
        return mount(ListingDetail, {
            props: {
                listing: listingData
            },
            global: {
                stubs: {
                    BaseBadge: {
                        template: '<span><slot /></span>'
                    },

                    VChip: {
                        template: '<span><slot /></span>'
                    },

                    VBtn: {
                        template: '<button><slot /></button>'
                    }
                }
            }
        })
    }

    it('renders listing information', () => {
        const wrapper = mountListingDetail() 
        
        expect(wrapper.find('[data-test="listing-title"]').text()).toBe('Catan for Sale')
        expect(wrapper.find('[data-test="listing-name"]').text()).toBe('Catan')
        expect(wrapper.find('[data-test="listing-price"]').text()).toContain('R500')
        expect(wrapper.find('[data-test="listing-username"]').text()).toBe('addyd')
        expect(wrapper.find('[data-test="listing-location"]').text()).toContain('Pretoria')
        expect(wrapper.find('[data-test="listing-description"]').text()).toBe('A lightly used copy of Catan.')
    })

    it('displays For Sale for a sale listing', () => {
        const wrapper = mountListingDetail() 

        expect(wrapper.find('[data-test="listing-badge"]').text()).toBe('For Sale')
    })

    it('displays For Rent for a rent listing', () => {
        const rentalListing = {
            ...listing,
            listingType: 'rental',
            rentalPeriod: {
                startDate: '2026-08-20',
                endDate: '2026-08-27'
            }
        }

        const wrapper = mountListingDetail(rentalListing) 

        expect(wrapper.find('[data-test="listing-badge"]').text()).toBe('For Rent')
        expect(wrapper.find('[data-test="rental-period"]').text()).toContain('2026-08-20')
        expect(wrapper.find('[data-test="rental-period"]').text()).toContain('2026-08-27')
    })

    it('renders listing genres', () => {
        const wrapper = mountListingDetail()

        const genres = wrapper.find('[data-test="listing-genres"]')

        expect(genres.exists()).toBe(true)
        expect(genres.text()).toContain('Strategy')
        expect(genres.text()).toContain('Family')
    })


    it('uses unknown when username is missing', () => {
        const wrapper = mountListingDetail({
            ...listing,
            username: undefined
        })

        expect(wrapper.find('[data-test="listing-username"]').text()).toBe('@unknown')
    })

    it('renders the contact seller button', () => {
        const wrapper = mountListingDetail()

        expect(wrapper.find('[data-test="contact-seller-button"]').exists()).toBe(true)
    })
})