import { beforeEach, describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

import LandingCallToAction from '~/components/features/landing/LandingCallToAction.vue'

describe('LandingCallToAction', () => {
  const push = vi.hoisted(() => vi.fn())

  mockNuxtImport('useRouter', () => () => ({
    push
  }))

  beforeEach(() => {
    push.mockClear()
  })
  
  const mountComponent = () =>
    mount(LandingCallToAction, {
      global: {
        stubs: {
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          VContainer: {
            template: '<div><slot /></div>'
          },
          VSheet: {
            template: '<div><slot /></div>'
          },
          VRow: {
            template: '<div><slot /></div>'
          },
          VCol: {
            template: '<div><slot /></div>'
          }
        }
      }
    })

  it('renders the call to action content', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Ready to play?')
    expect(wrapper.text()).toContain('Spend less time reading.')
    expect(wrapper.text()).toContain('More time playing.')
  })

  it('renders both action buttons', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Rulebooks')
    expect(wrapper.text()).toContain('Get Started')
  })

  it.each([
    ['Rulebooks', '/library'],
    ['Get Started', '/auth/signup']
  ])('navigates to %s', async (label, route) => {
    const wrapper = mountComponent()

    const buttons = wrapper.findAll('button')
    const button = buttons.find(item => item.text() === label)

    await button?.trigger('click')

    expect(push).toHaveBeenCalledWith(route)
  })
})