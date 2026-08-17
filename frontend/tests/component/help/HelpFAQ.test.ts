import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import HelpFAQ from '~/components/features/help/HelpFAQ.vue'

describe('HelpFAQ', () => {
  const mountComponent = ()  => 
    mount(HelpFAQ, {
      global: {
        stubs: {
          VContainer: {
            template: '<div><slot /></div>'
          },
          VExpansionPanels: {
            template: '<div><slot /></div>'
          },
          VExpansionPanel: {
            template: '<div class="v-expansion-panel"><slot /></div>'
          },
          VExpansionPanelTitle: {
            template: '<div><slot /></div>'
          },
          VExpansionPanelText: {
            template: '<div><slot /></div>'
          }
        }
      }
    })

  it('renders the FAQ heading', () => {

    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Frequently Asked Questions'
    )

  })

  it('renders FAQ expansion panels', () => {

    const wrapper = mountComponent()

    expect(
      wrapper.findAll('.v-expansion-panel').length
    ).toBeGreaterThan(0)

  })

})