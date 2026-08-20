import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ExploreTabs from '~/components/features/community/ExploreTabs.vue'

describe('ExploreTabs', () => {
  it('renders Chat and About tabs', () => {
    const wrapper = mount(ExploreTabs, {
      props: {
        activeTab: 'Chat'
      },
      global: {
        stubs: {
          BaseTabs: {
            props: ['tabs', 'activeTab'],
            template: `
              <div>
                <button
                  v-for="tab in tabs"
                  :key="tab"
                >
                  {{ tab }}
                </button>
              </div>
            `
          }
        }
      }
    })

    expect(wrapper.text()).toContain('Chat')
    expect(wrapper.text()).toContain('About')
  })

  it('uses Chat as the default active tab', () => {
    const wrapper = mount(ExploreTabs, {
      global: {
        stubs: {
          BaseTabs: {
            props: ['activeTab'],
            template: '<div>{{ activeTab }}</div>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('Chat')
  })

  it('emits change when the tab changes', async () => {
    const wrapper = mount(ExploreTabs, {
      props: {
        activeTab: 'Chat'
      },
      global: {
        stubs: {
          BaseTabs: {
            emits: ['change'],
            template: `
              <button
                @click="$emit('change', 'About')"
              >
                Change
              </button>
            `
          }
        }
      }
    })

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('change')).toEqual([
      ['About']
    ])
  })
})