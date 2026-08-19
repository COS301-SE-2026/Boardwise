import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

import SpecificItemHero from '~/components/features/specific-item/SpecificItemHero.vue'

const push = vi.fn()
const back = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push,
    back
  })
}))

describe('SpecificItemHero', () => {
  beforeEach(() => {
    push.mockClear()
    back.mockClear()
  })

  const listing = {
    id: 'listing-1',
    listingTitle: 'Catan Board Game',
    imageUrl: '/images/catan.jpg',
    listingType: 'sale',
    price: 750,
    username: 'lesa',
    location: 'Pretoria',
    description: 'A complete Catan board game.',
    isNegotiable: true,
    rulebookId: 'catan-rulebook'
  }

  const mountComponent = (listingOverride = {}) =>
    mount(SpecificItemHero, {
      props: {
        listing: {
          ...listing,
          ...listingOverride
        }
      },
      global: {
        stubs: {
          BaseImage: {
            props: ['src', 'alt', 'height'],
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
          },

          BaseButton: {
            props: ['variant'],
            template: `
              <button
                data-test="button"
                :data-variant="variant"
                @click="$emit('click')"
              >
                <slot />
              </button>
            `
          },

          ContactListerModal: {
            props: ['modelValue', 'listingTitle'],
            template: `
              <div
                data-test="contact-modal"
                :data-open="modelValue"
                :data-title="listingTitle"
              />
            `
          }
        }
      }
    })

  it('renders the listing title', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h1').text()).toBe('Catan Board Game')
  })

  it('renders the listing image', () => {
    const wrapper = mountComponent()

    const image = wrapper.find('[data-test="image"]')

    expect(image.exists()).toBe(true)
    expect(image.attributes('src')).toBe('/images/catan.jpg')
    expect(image.attributes('alt')).toBe('Catan Board Game')
  })

  it('renders the sale badge for a sale listing', () => {
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

  it('renders the listing price', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('.price').text()).toContain('R750')
  })

  it('renders rental dates for a rental listing', () => {
    const wrapper = mountComponent({
      listingType: 'rental',
      rentalPeriod: ['10 August', '20 August']
    })

    expect(wrapper.text()).toContain('10 August to 20 August')
  })

  it('does not render rental dates for an invalid rental period', () => {
    const wrapper = mountComponent({
      listingType: 'rental',
      rentalPeriod: ['10 August']
    })

    expect(wrapper.text()).not.toContain('10 August to')
  })

  it('renders the username and location', () => {
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

  it('renders the description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('A complete Catan board game.')
  })

  it('uses a fallback when description is missing', () => {
    const wrapper = mountComponent({
      description: null
    })

    expect(wrapper.text()).toContain('No description provided.')
  })

  it('renders the negotiation message when the listing is negotiable', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Open to negotiation')
  })

  it('does not render the negotiation message when not negotiable', () => {
    const wrapper = mountComponent({
      isNegotiable: false
    })

    expect(wrapper.text()).not.toContain('Open to negotiation')
  })

  it('opens the contact modal when Contact lister is clicked', async () => {
    const wrapper = mountComponent()

    const contactButton = wrapper
      .findAll('[data-test="button"]')
      .find(button => button.text() === 'Contact lister')

    await contactButton!.trigger('click')

    expect(wrapper.find('[data-test="contact-modal"]').attributes('data-open'))
      .toBe('true')
  })

  it('passes the listing title to the contact modal', () => {
    const wrapper = mountComponent()

    expect(
      wrapper.find('[data-test="contact-modal"]').attributes('data-title')
    ).toBe('Catan Board Game')
  })

  it('navigates to the rulebook when Read rulebook is clicked', async () => {
    const wrapper = mountComponent()

    const rulebookButton = wrapper
      .findAll('[data-test="button"]')
      .find(button => button.text() === 'Read rulebook')

    await rulebookButton!.trigger('click')

    expect(push).toHaveBeenCalledWith('/library/catan-rulebook')
  })

  it('goes back when Go back is clicked', async () => {
    const wrapper = mountComponent()

    const backButton = wrapper
      .findAll('[data-test="button"]')
      .find(button => button.text() === 'Go back')

    await backButton!.trigger('click')

    expect(back).toHaveBeenCalled()
  })
})