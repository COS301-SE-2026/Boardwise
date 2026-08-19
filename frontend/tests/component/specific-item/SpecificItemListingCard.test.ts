import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

import SpecificItemListingCard from '~/components/features/specific-item/SpecificItemListingCard.vue'

const mockRouter = {
  push: vi.fn()
}

vi.mock('vue-router', () => ({
  useRouter: () => mockRouter
}))

describe('SpecificItemListingCard', () => {
  beforeEach(() => {
    mockRouter.push.mockClear()
  })

  const listing = {
    id: 'listing-123',
    listingTitle: 'Catan Board Game',
    imageUrl: '/images/catan.jpg',
    listingType: 'sale',
    price: 750,
    username: 'lesa',
    location: 'Pretoria'
  }

  const mountComponent = (listingOverride = {}) =>
    mount(SpecificItemListingCard, {
      props: {
        listing: {
          ...listing,
          ...listingOverride
        }
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div data-test="card"><slot /></div>'
          },

          BaseImage: {
            props: ['src', 'alt'],
            template: `
              <img
                data-test="image"
                :src="src"
                :alt="alt"
              />
            `
          },

          BaseBadge: {
            props: ['variant'],
            template: `
              <span data-test="badge" :data-variant="variant">
                <slot />
              </span>
            `
          }
        }
      }
    })

  it('renders the listing title', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h3').text()).toBe('Catan Board Game')
  })

  it('renders the listing image', () => {
    const wrapper = mountComponent()

    const image = wrapper.find('[data-test="image"]')

    expect(image.exists()).toBe(true)
    expect(image.attributes('src')).toBe('/images/catan.jpg')
    expect(image.attributes('alt')).toBe('Catan Board Game')
  })

  it('renders the price', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('R750')
  })

  it('renders the seller and location', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('@lesa')
    expect(wrapper.text()).toContain('Pretoria')
  })

  it('uses unknown when username is missing', () => {
    const wrapper = mountComponent({
      username: null
    })

    expect(wrapper.text()).toContain('@unknown')
  })

  it('renders the sale badge', () => {
    const wrapper = mountComponent()

    const badge = wrapper.find('[data-test="badge"]')

    expect(badge.attributes('data-variant')).toBe('sale')
    expect(badge.text()).toBe('For sale')
  })

  it('renders the rental badge for a rental listing', () => {
    const wrapper = mountComponent({
      listingType: 'rental'
    })

    const badge = wrapper.find('[data-test="badge"]')

    expect(badge.attributes('data-variant')).toBe('rent')
    expect(badge.text()).toBe('For rent')
  })

  it('renders rental dates', () => {
    const wrapper = mountComponent({
      listingType: 'rental',
      rentalPeriod: ['10 August', '20 August']
    })

    expect(wrapper.text()).toContain('10 August to 20 August')
  })

  it('navigates to the specific item when clicked', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.listing-card').trigger('click')

    expect(mockRouter.push).toHaveBeenCalledWith('/specific-item/listing-123')
  })
})