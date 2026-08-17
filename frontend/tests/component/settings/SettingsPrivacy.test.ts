import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SettingsPrivacy from '~/components/features/settings/SettingsPrivacy.vue'

describe('SettingsPrivacy', () => {
  const mountComponent = () =>
    mount(SettingsPrivacy, {
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot/></button>'
          },
          VSelect: {
            name: 'VSelect',
            template: '<select><slot /></select>'
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
  it('renders the privacy heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Privacy'
    )
  })

  it('renders the profile visibility options', () => {
    const wrapper = mountComponent()

    const select = wrapper.findComponent({ name: 'VSelect' })

    expect(select.exists()).toBe(true)
  })

  it('renders notification preferences', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Show Online Status'
    )
    expect(wrapper.text()).toContain(
      'Show Activity'
    )
    expect(wrapper.text()).toContain(
      'Show Events'
    )
    expect(wrapper.text()).toContain(
      'Show Marketplace'
    )
  })

  it('renders the Save Privacy Settings button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Save Privacy Settings'
    )
  })

  it('emits privacy settings when saved', async() => {
    const wrapper = mountComponent()

    await wrapper.get('button').trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()

    expect(wrapper.emitted('save')?.[0]?.[0]).toEqual(
      expect.objectContaining({
        visibility: expect.any(String),
        Settings: expect.any(Object)
      })
    )
  })

})