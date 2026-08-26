import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarketplaceSearch from '~/components/features/marketplace/MarketplaceSearch.vue'

describe('MarketplaceSearch.vue', () => {
    it('renders the search and create listings controls', () => {
        const wrapper = mount(MarketplaceSearch, {
            global: {
                stubs: {
                    BaseSearch: {
                        template: '<input />'
                    },
                    BaseButton: {
                        template: '<button><slot /></button>'
                    }
                } 
            }
        })

        expect(wrapper.find('[data-test="marketplace-search"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="create-listing-button"]').exists()).toBe(true)
    })

    it('emits search when the seatch value changes', async () => {
        const wrapper = mount(MarketplaceSearch, {
            global: {
                stubs: {
                    BaseSearch: {
                        props: ['modelValue'],
                        emits: ['update:ModelValue'],
                        template: `
                            <input 
                                :value="modelValue"
                                @input="$emit('update:modelValue', $event.target.value)"
                            />
                        `
                    },

                    BaseButton: {
                        template: '<button><slot /></button>'
                    }
                }
            }
        })
        
        const search = wrapper.find('[data-test="marketplace-search"] input')

        await search.setValue('Catan')

        expect(wrapper.emitted('search')).toBeTruthy()
        expect(wrapper.emitted('search')?.[0]).toEqual(['Catan'])
    })

    it('emits create-listing when the create listing button is clicked', async () => {
        const wrapper = mount(MarketplaceSearch, {
            global: {
                stubs: {
                    BaseSearch: {
                        template: '<input />'
                    }, 

                    BaseButton: {
                        template: `
                            <button @click="$emit('click')"> 
                                <slot />
                            </button>
                        `,
                        emits: ['click']
                    }
                }
            }
        })

        await wrapper.find('[data-test="create-listing-button"]').trigger('click')

        expect(wrapper.emitted('create-listing')).toBeTruthy()
    })
})