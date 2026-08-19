import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

import SpecificItemRulebook from '~/components/features/specific-item/SpecificItemRulebook.vue'

const push = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push
  })
}))

describe('SpecificItemRulebook', () => {
  beforeEach(() => {
    push.mockClear()
  })

  const rulebook = {
    id: 'catan-rulebook',
    title: 'Catan',
    pages: [
      {
        title: 'Overview',
        content: 'Catan is a strategy game about collecting resources.'
      },
      {
        title: 'Setup',
        content: 'Place the board and distribute the starting pieces.'
      },
      {
        title: 'Gameplay',
        content: 'Players take turns rolling dice and trading resources.'
      }
    ]
  }

  const mountComponent = (rulebookOverride = {}) =>
    mount(SpecificItemRulebook, {
      props: {
        rulebook: {
          ...rulebook,
          ...rulebookOverride
        }
      },
      global: {
        stubs: {
          SectionTitle: {
            props: ['title'],
            template: '<h2 data-test="title">{{ title }}</h2>'
          },

          BaseTabs: {
            props: ['tabs', 'activeTab'],
            emits: ['change'],
            template: `
              <div data-test="tabs">
                <button
                  v-for="tab in tabs"
                  :key="tab"
                  :data-tab="tab"
                  @click="$emit('change', tab)"
                >
                  {{ tab }}
                </button>
              </div>
            `
          },

          BaseCard: {
            template: '<div data-test="card"><slot /></div>'
          },

          BaseButton: {
            template: `
              <button data-test="button" @click="$emit('click')">
                <slot />
              </button>
            `
          }
        }
      }
    })

  it('renders the rulebook title', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="title"]').text())
      .toBe('Catan — Rulebook')
  })

  it('renders all rulebook page tabs', () => {
    const wrapper = mountComponent()

    const tabs = wrapper.findAll('[data-test="tabs"] button')

    expect(tabs).toHaveLength(3)
    expect(tabs.map(tab => tab.text())).toEqual([
      'Overview',
      'Setup',
      'Gameplay'
    ])
  })

  it('shows the first page by default', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="card"]').text())
      .toContain('Catan is a strategy game about collecting resources.')
  })

  it('changes the displayed page when a tab is selected', async () => {
    const wrapper = mountComponent()

    await wrapper
      .find('[data-test="tabs"] [data-tab="Setup"]')
      .trigger('click')

    expect(wrapper.find('[data-test="card"]').text())
      .toContain('Place the board and distribute the starting pieces.')
  })

  it('renders the View full rulebook button', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="button"]').text())
      .toBe('View full rulebook')
  })

  it('navigates to the full rulebook', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-test="button"]').trigger('click')

    expect(push).toHaveBeenCalledWith('/library/catan-rulebook')
  })

  it('renders nothing when no rulebook is provided', () => {
    const wrapper = mount(SpecificItemRulebook, {
      props: {
        rulebook: null
      },
      global: {
        stubs: {
          SectionTitle: true,
          BaseTabs: true,
          BaseCard: true,
          BaseButton: true
        }
      }
    })

    expect(wrapper.find('.rulebook').exists()).toBe(false)
  })
})