import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RetailerGrid from '~/components/features/marketplace/RetailerGrid.vue'

describe('RetailerGrid.vue', () => {

    const retailers = [
        {
            retailerName: 'Takealot',
            retailTitle: 'Catan Board Game',
            price: 899,
            imageUrl: '/catan.png',
            url: 'https://example.com/catan'
        },

        {
            retailerName: 'Board Game Store',
            retailTitle: 'Ticket to Ride',
            price: 799,
            url: 'https://example.com/ticket-to-ride'
        }

    ]

    it('renders retailer card for each retailer', () => {
        const wrapper = mount(RetailerGrid, {
            props: {
                retailers
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    RetailerCard: {
                        template: '<div data-test"retailer-card"></div>',
                    }
                } 
            }
        })

        const cards = wrapper.findAll('[data-test="retailer-card"]')

        expect(cards).toHaveLength(2)
    })

    it('renders no retailer cards when there are no retailers', () => {
        const wrapper = mount(RetailerGrid, {
            props: {
                retailers: []
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    RetailerCard: {
                        template: '<div data-test"retailer-card"></div>',
                    }
                } 
            }
        })

        expect(wrapper.findAll('[data-test="retailer-card"]')).toHaveLength(0)
    })

    it('passes each retailer to the retailer card', () => {
        const wrapper = mount(RetailerGrid, {
            props: {
                retailers
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    RetailerCard: {
                        props: ['retail'],
                        template:`
                            <div data-test="retailer-card">
                                {{ retail.retailerName }}
                            </div>
                        `
                    }
                } 
            }
        })

        const cards = wrapper.findAll('[data-test="retailer-card"]')

        expect(cards[0]?.text()).toBe('Takealot')
        expect(cards[1]?.text()).toBe('Board Game Store')
    })

})