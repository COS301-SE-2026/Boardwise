import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import HelpView from '~/components/features/help/HelpView.vue'

import Navbar from '~/components/layout/Navbar.vue'
import HelpHero from '~/components/features/help/HelpHero.vue'
import HelpTutorials from '~/components/features/help/HelpTutorials.vue'
import HelpFAQ from '~/components/features/help/HelpFAQ.vue'
import HelpContact from '~/components/features/help/HelpContact.vue'

describe('HelpView', () => {
  const mountComponent = () =>
      mount(HelpView, {
        global: {
          stubs: {
            PageContainer: {
              template: '<div><slot /></div>'
            },
            Navbar: {
              template: '<div><slot /></div>'
            },
            HelpHero: {
              template: '<div><slot /></div>'
            },
            HelpTutorials: {
              template: '<div><slot /></div>'
            },
            HelpFAQ: {
              template: '<div><slot /></div>'
            },
            HelpContact: {
              template: '<div><slot /></div>'
            }
          }
        }
      })
  it('renders the Help page layout correctly', () => {

    const wrapper = mountComponent()

    expect(wrapper.findComponent(Navbar).exists()).toBe(true)
    expect(wrapper.findComponent(HelpHero).exists()).toBe(true)
    expect(wrapper.findComponent(HelpTutorials).exists()).toBe(true)
    expect(wrapper.findComponent(HelpFAQ).exists()).toBe(true)
    expect(wrapper.findComponent(HelpContact).exists()).toBe(true)

  })

})