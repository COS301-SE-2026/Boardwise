import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import LandingFeatures from '~/components/features/landing/LandingFeatures.vue'

describe('LandingFeatures', () => {
  const mountComponent = () =>
    mount(LandingFeatures, {
      global: {
        stubs: {
          VContainer: {
            template: '<div><slot /></div>'
          },
          VRow: {
            template: '<div><slot /></div>'
          },
          VCol: {
            template: '<div><slot /></div>'
          },
          LandingFeatureCard: {
            props: ['feature'],
            template: `
              <div class="feature-card">
                {{ feature.title }}
              </div>
            `
          }
        }
      }
    })

  it('renders the features heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Everything Boardwise Offers'
    )
  })

  it('renders the features description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Everything you need to discover, collect and enjoy board games.'
    )
  })

  it('renders feature cards', () => {
    const wrapper = mountComponent()

    expect(
      wrapper.findAll('.feature-card').length
    ).toBeGreaterThan(0)
  })
})