import { beforeEach, describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

import LandingNavbar from '~/components/features/landing/LandingNavbar.vue'

describe('LandingNavbar', () => {
  const push = vi.hoisted(() => vi.fn())

    mockNuxtImport('useRouter', () => () => ({
    push
  }))

  beforeEach(() => {
    push.mockClear()
  })
  
  const mountComponent = () =>
    mount(LandingNavbar, {
      global: {
        mocks: {
          useRouter: () => ({ push })
        },
        stubs: {
          VAppBar: {
            template: '<header><slot /></header>'
          },
          VContainer: {
            template: '<div><slot /></div>'
          },
          VSpacer: {
            template: '<span />'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

  it('renders the Boardwise brand', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Boardwise')
  })

  it('renders Sign In and Sign Up buttons', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Sign In')
    expect(wrapper.text()).toContain('Sign Up')
  })

  it.each([
    ['Sign In', '/auth/signin'],
    ['Sign Up', '/auth/signup']
  ])('navigates to %s', async (label, route) => {
    const wrapper = mountComponent()

    const button = wrapper.findAll('button')
      .find(item => item.text() === label)

    await button?.trigger('click')

    expect(push).toHaveBeenCalledWith(route)
  })
})