import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarketplaceHeader from '~/components/features/marketplace/MarketplaceHeader.vue'

describe('MarketplaceHeader.vue', () => {

    it('renders the marketplace title and search section', () => {
        const wrapper = mount(MarketplaceHeader, {
            global: {
                stubs: {
                    SectionTitle: {
                        props: ['title', 'subtitle'],
                        template: `
                            <div data-test="marketplace-title">
                                <h1>{{ title }}</h1>
                                <p>{{ subtitle }}</p>
                            </div>
                        `
                    },
                    MarketplaceSearch: {
                        template: '<div data-test="marketplace-search"></div>'
                    }
                }
            }
        })

        expect(wrapper.find('[data-test="marketplace-header"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="marketplace-title"]').exists()).toBe(true)
        expect(wrapper.text()).toContain('Marketplace')
        expect(wrapper.text()).toContain('Buy, Rent and List board games with the community')
        expect(wrapper.find('[data-test="marketplace-search"]').exists()).toBe(true)
    })

    it('emits search when MarketplaceSearch emits search', async () => {
        const wrapper = mount(MarketplaceHeader, {
            global: {
                stubs: {
                    SectionTitle: true,
                    MarketplaceSearch: {
                        emits: ['search'],
                        template: `
                            <button
                                data-test="marketplace-search"
                                @click="$emit('search', 'Catan')"
                            >
                                Search
                            </button>
                        `
                    }
                }
            }
        })

        await wrapper
            .find('[data-test="marketplace-search"]')
            .trigger('click')

        expect(wrapper.emitted('search')).toBeTruthy()

        expect(
            wrapper.emitted('search')?.[0]
        ).toEqual(['Catan'])
    })

    it('emits create-listing when MarketplaceSearch emits create-listing', async () => {
        const wrapper = mount(MarketplaceHeader, {
            global: {
                stubs: {
                    SectionTitle: true,
                    MarketplaceSearch: {
                        emits: ['create-listing'],
                        template: `
                            <button
                                data-test="marketplace-search"
                                @click="$emit('create-listing')"
                            >
                                Create Listing
                            </button>
                        `
                    }
                }
            }
        })

        await wrapper
            .find('[data-test="marketplace-search"]')
            .trigger('click')

        expect(
            wrapper.emitted('create-listing')
        ).toBeTruthy()
    })
})