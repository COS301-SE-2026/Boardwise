import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SettingsView from '~/components/features/settings/SettingsView.vue'

describe('SettingsView', () => {
  const mountComponent = () =>
    mount(SettingsView, {
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          VList: {
            template: '<div><slot /></div>'
          },
          VListItem: {
            props: ['active'],
            template: `
              <button
                :class="{ active }"
                @click="$emit('click')"
              >
                <slot />
              </button>`
          },
          VListItemTitle: {
            template: '<span><slot /></span>'
          },
          SettingsAppearance: {
            template: '<div data-test="appearance">Appearance</div>'
          },
          SettingsPrivacy: {
            template: '<div data-test="privacy">Privacy</div>'
          },
          SettingsNotifications: {
            template: '<div data-test="notifications">Notifications</div>'
          }
        }
      }
    })

  it('renders the Settings heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Settings'
    )
  })

  it('renders all settings sections', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Appearance'
    )
    expect(wrapper.text()).toContain(
      'Privacy'
    )
    expect(wrapper.text()).toContain(
      'Notifications'
    )

  })

  it.each([
    ['Appearance' , 0 , 'appearance'],
    ['Privacy' , 1 , 'privacy'],
    ['Notifications' , 2 , 'notifications'],
  ])(
    'shows %s when it is selected', 
    async(_section, index, testId) => {
    const wrapper = mountComponent()

    const items = wrapper.findAll('button')

    await items[index]?.trigger('click')

    expect(wrapper.find(`[data-test="${testId}"]`).exists()).toBe(true)
  
  })
  
})