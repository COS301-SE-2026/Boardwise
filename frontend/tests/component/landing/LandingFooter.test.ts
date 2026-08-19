import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import LandingFooter from '~/components/features/landing/LandingFooter.vue'

describe('LandingFooter', () => {
  const mountComponent = () =>
    mount(LandingFooter, {
      global: {
        stubs: {
          VFooter: {
            template: '<footer><slot /></footer>'
          },
          VContainer: {
            template: '<div><slot /></div>'
          },
          VRow: {
            template: '<div><slot /></div>'
          },
          VCol: {
            template: '<div><slot /></div>'
          },
          VImg: {
            template: '<img :alt="alt" />',
            props: ['alt']
          },
          NuxtLink: {
            template: '<a><slot /></a>'
          }
        }
      }
    })

  it('renders the Boardwise footer', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Boardwise')
    expect(wrapper.text()).toContain(
      'Discover, collect and share board games with the Boardwise community.'
    )
  })

  it('renders the Explore section', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Explore')
    expect(wrapper.text()).toContain('Library')
    expect(wrapper.text()).toContain('Marketplace')
    expect(wrapper.text()).toContain('Communities')
    expect(wrapper.text()).toContain('Events')
    expect(wrapper.text()).toContain('Help')
  })

  it('renders the Account section', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Account')
    expect(wrapper.text()).toContain('Sign In')
    expect(wrapper.text()).toContain('Sign Up')
  })

  it('renders the BoardGameGeek attribution', () => {
    const wrapper = mountComponent()

    expect(
      wrapper.find('img[alt="Powered by BoardGameGeek"]').exists()
    ).toBe(true)
  })
})