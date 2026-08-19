import { beforeEach ,describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

import LandingHero from '~/components/features/landing/LandingHero.vue'

describe('LandingHero', () => {
  const push = vi.hoisted(() => vi.fn())

  mockNuxtImport('useRouter', () => () => ({
    push
  }))

  beforeEach(() => {
    push.mockClear()
  })
  
  const mountComponent = () =>
    mount(LandingHero, {
      global: {
        stubs: {
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          VContainer: {
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

  it('renders the hero heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Play more. Learn faster.')
    expect(wrapper.text()).toContain(
      'For people who love the table.'
    )
  })

  it('renders the hero badges', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Connect')
    expect(wrapper.text()).toContain('Share')
    expect(wrapper.text()).toContain('Play')
  })

  it('renders the hero description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Access a library of your favourite board games'
    )
  })

  it.each([
    ['Rulebooks', '/library'],
    ['Get started', '/auth/signup']
  ])('navigates when %s is clicked', async (label, route) => {
    const wrapper = mountComponent()

    const button = wrapper.findAll('button')
      .find(item => item.text() === label)

    await button?.trigger('click')

    expect(push).toHaveBeenCalledWith(route)
  })
})