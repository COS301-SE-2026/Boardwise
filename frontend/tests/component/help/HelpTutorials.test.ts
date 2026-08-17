import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

import HelpTutorials from '~/components/features/help/HelpTutorials.vue'


const push = vi.fn()

vi.mock('#app/composables/router', () => ({
  useRouter: () => ({
    push
  })
}))

describe('HelpTutorials', () => {

  beforeEach(() => {
    push.mockClear()
  })

  it('renders the section title', () => {

    const wrapper = mount(HelpTutorials, {
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
          VCard: {
            template: '<div class="v-card"><slot /></div>'
          },
          VCardTitle: {
            template: '<div><slot /></div>'
          },
          VCardText: {
            template: '<div><slot /></div>'
          },
          VBtn: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
      })

    expect(wrapper.text()).toContain(
      'Quick Start Guides'
    )

  })

  it('renders tutorial cards', () => {

    const wrapper = mount(HelpTutorials, {
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
          VCard: {
            template: '<div class="v-card"><slot /></div>'
          },
          VCardTitle: {
            template: '<div><slot /></div>'
          },
          VCardText: {
            template: '<div><slot /></div>'
          },
          VBtn: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }}
    )

    expect(wrapper.findAll('.v-card').length)
      .toBeGreaterThan(0)

  })

  it('navigates when a tutorial button is clicked', async () => {

    const wrapper = mount(HelpTutorials, {
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
          VCard: {
            template: '<div class="v-card"><slot /></div>'
          },
          VCardTitle: {
            template: '<div><slot /></div>'
          },
          VCardText: {
            template: '<div><slot /></div>'
          },
          VBtn: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
  })

    const button = wrapper.find('button')

    expect(button.exists()).toBe(true)

    await button.trigger('click')

    expect(push).toHaveBeenCalled()

  })

})