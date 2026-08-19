import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import LandingGameStrip from '~/components/features/landing/LandingGameStrip.vue'

describe('LandingGameStrip', () => {
  const mountComponent = () =>
    mount(LandingGameStrip, {
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseImage: {
            props: ['src', 'alt'],
            template: '<img :src="src" :alt="alt" />'
          },
          NuxtLink: {
            props: ['to'],
            template: '<a :href="to"><slot /></a>'
          }
        }
      }
    })

  it('renders the game strips', () => {
    const wrapper = mountComponent()

    expect(
      wrapper.findAll('.game-strip')
    ).toHaveLength(3)
  })

  it('renders game cards', () => {
    const wrapper = mountComponent()

    expect(
      wrapper.findAll('.game-card').length
    ).toBeGreaterThan(0)
  })

  it('renders game images', () => {
    const wrapper = mountComponent()

    expect(
      wrapper.findAll('img').length
    ).toBeGreaterThan(0)
  })
})