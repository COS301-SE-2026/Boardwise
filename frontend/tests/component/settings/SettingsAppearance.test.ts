import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SettingsAppearance from '~/components/features/settings/SettingsAppearance.vue'

describe('SettingsAppearance', () => {
  const mountComponent = () =>
    mount(SettingsAppearance, {
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
  it('renders the appearance heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Appearance'
    )
  })

  it('renders all theme options', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Light Mode'
    )
    expect(wrapper.text()).toContain(
      'Dark Mode'
    )
    expect(wrapper.text()).toContain(
      'System Default'
    )

  })

  it('renders the Apply button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'Apply'
    )
  })

  it('emits the selected theme when Apply is clicked', async() => {
    const wrapper = mountComponent()

    await wrapper.get('button').trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()

    expect(wrapper.emitted('save')?.[0]).toEqual([
      { theme: expect.any(String) }
    ])
  })

})