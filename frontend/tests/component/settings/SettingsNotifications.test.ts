import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SettingsNotifications from '~/components/features/settings/SettingsNotifications.vue'
describe('SettingsNotifications', () => {
  const mountComponent = () =>
    mount(SettingsNotifications, {
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot/></button>'
          },
          VList: {
            template: '<div><slot /></div>'
          },
          VListItem: {
            template: '<div><slot /></div>'
          },
          VSwitch: {
            props: ['modelValue'],
            template: '<input type="checkbox" />'
          }
        }
      }
    })
  it('renders the notifications heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Notifications'
    )
  })

  it('renders notification preferences', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Event RSVPs'
    )
    expect(wrapper.text()).toContain(
      'Friend Requests'
    )
    expect(wrapper.text()).toContain(
      'Marketplace Interests'
    )
    expect(wrapper.text()).toContain(
      'Vault Updates'
    )
    expect(wrapper.text()).toContain(
      'Community Events'
    )

  })

  it('renders the Save Preferences button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Save Preferences'
    )
  })

  it('emits the selected theme when Apply is clicked', async() => {
    const wrapper = mountComponent()

    await wrapper.get('button').trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()

    expect(wrapper.emitted('save')?.[0]?.[0]).toEqual(
      expect.objectContaining({
        event_rsvp: expect.any(Boolean),
        friend_request: expect.any(Boolean),
        marketplace_interest: expect.any(Boolean),
        vault_update: expect.any(Boolean),
        community_event: expect.any(Boolean),
      })
    )
  })

})