import { beforeEach, describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

import LandingFeatureCard from '~/components/features/landing/LandingFeatureCard.vue'

describe('LandingFeatureCard', () => {
  const push = vi.hoisted(() => vi.fn())

  mockNuxtImport('useRouter', () => () => ({
    push
  }))

  beforeEach(() => {
    push.mockClear()
  })

  const feature = {
    id: 1,
    icon: 'mdi-dice-multiple',
    title: 'Board Games',
    description: 'Discover board games.',
    highlights: [
      'Find games',
      'Read rules',
      'Connect with players'
    ],
    route: '/library'
  }

  const mountComponent = () =>
    mount(LandingFeatureCard, {
      props: {
        feature
      },
      global: {
        mocks: {
          useRouter: () => ({ push })
        },
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          VHover: {
            template: '<div><slot :isHovering="false" /></div>'
          },
          VIcon: {
            template: '<span><slot /></span>'
          },
          VList: {
            template: '<div><slot /></div>'
          },
          VListItem: {
            template: '<div><slot /></div>'
          },
          VListItemTitle: {
            template: '<span><slot /></span>'
          },
          VSpacer: {
            template: '<span />'
          }
        }
      }
    })

  it('renders the feature title and description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Board Games')
    expect(wrapper.text()).toContain('Discover board games.')
  })

  it('renders all feature highlights', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Find games')
    expect(wrapper.text()).toContain('Read rules')
    expect(wrapper.text()).toContain('Connect with players')
  })

  it('renders the Explore action', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Explore')
  })

  it('navigates to the feature route', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.cursor-pointer').trigger('click')

    expect(push).toHaveBeenCalledWith('/library')
  })
})