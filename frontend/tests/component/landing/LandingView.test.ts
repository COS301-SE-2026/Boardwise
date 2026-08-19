import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import LandingView from '~/components/features/landing/LandingView.vue'

describe('LandingView', () => {
  const mountComponent = () =>
    mount(LandingView, {
      global: {
        stubs: {
          LandingNavbar: {
            template: '<div data-test="navbar">Navbar</div>'
          },
          LandingHero: {
            template: '<div data-test="hero">Hero</div>'
          },
          LandingGameStrip: {
            template: '<div data-test="game-strip">Game Strip</div>'
          },
          LandingFeatures: {
            template: '<div data-test="features">Features</div>'
          },
          LandingCallToAction: {
            template: '<div data-test="cta">CTA</div>'
          },
          LandingFooter: {
            template: '<div data-test="footer">Footer</div>'
          }
        }
      }
    })

  it('renders all landing sections', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="navbar"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="hero"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="game-strip"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="features"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="cta"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="footer"]').exists()).toBe(true)
  })

  it('renders the landing page content in the expected order', () => {
    const wrapper = mountComponent()

    const navbar = wrapper.find('[data-test="navbar"]')
    const main = wrapper.find('main')
    const footer = wrapper.find('[data-test="footer"]')

    expect(navbar.exists()).toBe(true)
    expect(main.exists()).toBe(true)
    expect(footer.exists()).toBe(true)

    const sections = main.findAll('[data-test]')

    expect(sections.map(section => section.attributes('data-test')))
      .toEqual([
        'hero',
        'game-strip',
        'features',
        'cta'
      ])
  })
})