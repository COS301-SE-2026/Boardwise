import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import HelpContact from '~/components/features/help/HelpContact.vue'

describe('HelpContact', () => {
  const mountComponent = () =>
    mount(HelpContact, {
      global: {
        stubs: {
          VContainer: {
            template: '<div><slot /></div>'
          },
          BaseButton: {
            props: ['href'],
            template: '<a :href="href"><slot/></a>'
          }
        }
      }
    })
  it('renders the support heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Still need help?'
    )
  })

  it('renders the Contact Support button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Contact Support'
    )

  })

  it('contains the support email link', () => {
    const wrapper = mountComponent()

    const link = wrapper.find('a')

    expect(link.exists()).toBe(true)

    expect(link.attributes('href'))
      .toContain('mailto:support@boardwise.co.za')

  })

})