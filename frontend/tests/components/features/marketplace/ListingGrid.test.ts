import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ListingsGrid from '~/components/features/marketplace/ListingsGrid.vue'

describe('ListingsGrid.vue', () => {

    const listings = [
        {
            listingId: 1,
            listingTitle: 'Catan for Sale',
            gameTitle: 'Catan',
            listingType: 'sale',
            price: 500
        },
        {
            listingId: 2,
            listingTitle: 'Ticket to Ride',
            gameTitle: 'Ticket to Ride',
            listingType: 'rental',
            price: 200
        }
    ]

    it('renders listing card for each listing', () => {
        const wrapper = mount(ListingsGrid, {
            props: {
                listings
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    ListingCard: {
                        template: '<div data-test"listing-card"></div>',
                    }
                } 
            }
        })

        const cards = wrapper.findAll('[data-test="listing-card"]')

        expect(cards).toHaveLength(2)
    })

    it('renders no listing cards when there are no listings', () => {
        const wrapper = mount(ListingsGrid, {
            props: {
                listings: []
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    listingCard: {
                        template: '<div data-test"listing-card"></div>',
                    }
                } 
            }
        })

        expect(wrapper.findAll('[data-test="listing-card"]')).toHaveLength(0)
    })

    it('passes each listing to the listing card', () => {
        const wrapper = mount(ListingsGrid, {
            props: {
                listings
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    listingCard: {
                        props: ['listing'],
                        template:`
                            <div data-test="listing-card">
                                {{ listing.gameTitle }}
                            </div>
                        `
                    }
                } 
            }
        })

        const cards = wrapper.findAll('[data-test="listing-card"]')

        expect(cards[0]?.text()).toBe('Catan')
        expect(cards[1]?.text()).toBe('Ticket to Ride')
    })

})