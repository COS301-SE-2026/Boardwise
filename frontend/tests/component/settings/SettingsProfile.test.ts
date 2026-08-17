import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SettingsProfile from '~/components/features/settings/SettingsProfile.vue'

 describe('SettingsProfile', () => {
  const mountComponent = () =>
    mount(SettingsProfile, {
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseInput: {
            props: ['modelValue', 'label', 'disabled'],
            template: `
            <div>
                <label>{{ label }}</label>
                <input :value="modelValue"
                  :disabled="disabled" />
            </div>`
          }
        }
      }
    })
  it('renders the profile information heading', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'General'
    )
    expect(wrapper.text()).toContain(
      'Your account information'
    )
  })

  it('renders the profile fields', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'First name'
    )
    expect(wrapper.text()).toContain(
      'Last Name'
    )
    expect(wrapper.text()).toContain(
      'Username'
    )
    expect(wrapper.text()).toContain(
      'Email'
    )
     expect(wrapper.text()).toContain(
      'Bio'
    )
  })

  it('renders the profile fields as disabled', () => {
    const wrapper = mountComponent()

    const inputs = wrapper.findAll('input, textarea')

    expect(inputs).toHaveLength(5)

    inputs.forEach(input => {
        expect(input.attributes('disabled')).toBeDefined()
    })
  })

})