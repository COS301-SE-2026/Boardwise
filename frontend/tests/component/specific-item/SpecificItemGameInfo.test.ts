import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SpecificItemGameInfo from '~/components/features/specific-item/SpecificItemGameInfo.vue'

describe('SpecificItemGameInfo', () => {
  const rulebook = {
    players: '2-4',
    duration: '60 minutes',
    age: '10+',
    difficulty: 'Medium',
    category: 'Strategy'
  }

  const mountComponent = (props = {}) =>
    mount(SpecificItemGameInfo, {
      props: {
        rulebook,
        ...props
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div data-test="card"><slot /></div>'
          },
          BaseFilterGroup: {
            props: ['title'],
            template: `
              <div data-test="filter-group">
                <h2>{{ title }}</h2>
                <slot />
              </div>
            `
          },
          BaseBadge: {
            template: '<span data-test="badge"><slot /></span>'
          }
        }
      }
    })

  it('renders the game details when a rulebook is provided', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="card"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="filter-group"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Game details')
  })

  it('renders the number of players', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Players')
    expect(wrapper.text()).toContain('2-4')
  })

  it('renders the duration', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Duration')
    expect(wrapper.text()).toContain('60 minutes')
  })

  it('renders the age requirement', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Age')
    expect(wrapper.text()).toContain('10+')
  })

  it('renders the difficulty as a badge', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Difficulty')
    expect(wrapper.find('[data-test="badge"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="badge"]').text()).toBe('Medium')
  })

  it('renders the category', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Category')
    expect(wrapper.text()).toContain('Strategy')
  })

  it('renders nothing when no rulebook is provided', () => {
    const wrapper = mountComponent({
      rulebook: null
    })

    expect(wrapper.find('[data-test="card"]').exists()).toBe(false)
  })
})