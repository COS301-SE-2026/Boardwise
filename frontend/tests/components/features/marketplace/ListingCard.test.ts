import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ListingCard from '~/components/features/marketplace/ListingCard.vue'

const push = vi.fn()

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push
    })
}))

describe('ListingCard.vue', () => {
    const listing = {
        listingId: 1,
        listingTitle: 'Catan for Sale',
        gameTitle: 'Catan',
        listingType: 'sale',
        price: 500,
        username: 'addyd',
        location: 'Pretoria',
        imageUrl: '/catan.png'
    }

    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders listing information', () => {
        const wrapper = mount(ListingCard, {
            props: {
                listing
            },

            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /><div>'
                    },
                    BaseImage: {
                        template: '<img :src="src" :alt="alt" />',
                        props: ['src', 'alt']
                    },
                    BaseBadge: {
                        template: '<span><slot /><span>'
                    }
                } 
            }
        })

        expect(wrapper.find('[data-test="listing-title"]').text()).toBe('Catan for Sale')
        expect(wrapper.find('[data-test="listing-name"]').text()).toBe('Catan')
        expect(wrapper.find('[data-test="listing-price"]').text()).toContain('R500')
        expect(wrapper.find('[data-test="listing-username"]').text()).toBe('addyd')
        expect(wrapper.find('[data-test="listing-location"]').text()).toContain('Pretoria')
    })

    it('displays For Sale for a sale listing', () => {
        const wrapper = mount(ListingCard, {
            props: {
                listing
            },
            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /></div>'
                    },
                    BaseImage: true,
                    BaseBadge: {
                        template: '<span><slot /></span>'
                    }
                }
            }
        })

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

        const wrapper = mount(ListingCard, {
            props: {
                listing: rentalListing
            },
            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /></div>'
                    },
                    BaseImage: true,
                    BaseBadge: {
                        template: '<span><slot /></span>'
                    }
                }
            }
        })

        expect(wrapper.find('[data-test="listing-badge"]').text()).toBe('For Rent')
        expect(wrapper.find('[data-test="rental-period"]').text()).toContain('2026-08-20')
        expect(wrapper.find('[data-test="rental-period"]').text()).toContain('2026-08-27')
    })

    it('opens the listing link in a new tab when clicked', async () => {
        const wrapper = mount(ListingCard, {
            props: {
                listing
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
                    BaseImage: true,
                    BaseBadge: true
                }
            }
        })

        await wrapper.find('[data-test="listing-card"]').trigger('click')

        expect(push).toHaveBeenCalledWith('/marketplace/1')
    })

    it('uses unknown when username is missing', () => {
        const wrapper = mount(ListingCard, {
            props: {
                listing: {
                    ...listing,
                    username: undefined
                }
            },
            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /></div>'
                    },
                    BaseImage: true,
                    BaseBadge: true
                }
            }
        })

        expect(wrapper.find('[data-test="listing-username"]').text()).toBe('@unknown')
    })
})