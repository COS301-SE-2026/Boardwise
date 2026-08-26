import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RetailerCard from '~/components/features/marketplace/RetailerCard.vue'
import { beforeEach } from 'node:test'

describe('RetailerCard.vue', () => {
    const retail = {
        retailerName: 'Takealot',
        retailTitle: 'Catan Board Game',
        price: 899,
        imageUrl: '/catan.png',
        url: 'https://example.com/catan'
    }

    beforeEach(() => {
        vi.stubGlobal('window', {
            open: vi.fn()
        })
    })

    it('renders retailer information', () => {
        const wrapper = mount(RetailerCard, {
            props: {
                retail
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    BaseImage: {
                        template: '<img :src="src" :alt="alt" />',
                        props: ['src', 'alt']
                    }
                } 
            }
        })

        expect(wrapper.find('[data-test="retailer-title"]').text()).toBe('Catan Board Game')
        expect(wrapper.find('[data-test="retailer-name"]').text()).toBe('Takealot')
        expect(wrapper.find('[data-test="retailer-price"]').text()).toBe('R899')
    })

    it('renders the retailer image', () => {
        const wrapper = mount(RetailerCard, {
            props: {
                retail
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    BaseImage: {
                        template: '<img :src="src" :alt="alt" />',
                        props: ['src', 'alt']
                    }
                } 
            }
        })
        
        const image = wrapper.find('[data-test="retailer-image"]')

        expect(image.exists()).toBe(true)
    })

    it('opens the retailer link in a new tab when clicked', async () => {
        const open = vi.fn()

        vi.stubGlobal('window', {
            open
        })

        const wrapper = mount(RetailerCard, {
            props: {
                retail
            },
            global: {
                stubs: {
                    BaseCard: {
                        template: `
                            <div @click="$emit('click')">
                                <slot />
                            </div>
                        `,
                        emits: ['click']
                    },
                    BaseImage: {
                        template: '<img />'
                    }
                }
            }
        })

        await wrapper.find('[data-test="retailer-card"]').trigger('click')

        expect(open).toHaveBeenCalledWith(
            'https://example.com/catan',
            '_blank',
            'noopener,noreferrer')
    })

    it('does not open a link when the retailer has no URL', async () => {
        const open = vi.fn()

        vi.stubGlobal('window', {
            open
        })

        const wrapper = mount(RetailerCard, {
            props: {
                retail: {
                    ...retail,
                    url: undefined
                }
            }, 
            global: {
                stubs: {
                    BaseCard: {
                        template: `
                            <div @click="$emit('click')">
                                <slot />
                            </div>
                        `,
                        emits: ['click']
                    },
                    BaseImage: {
                        template: '<img />'
                    }
                }
            }
        })

        await wrapper.find('[data-test="retailer-card"]').trigger('click')
        
        expect(open).not.toHaveBeenCalled()
    })
})