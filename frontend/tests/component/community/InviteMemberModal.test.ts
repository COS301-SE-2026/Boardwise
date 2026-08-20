import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import InviteMemberModal from '~/components/features/community/InviteMemberModal.vue'

describe('InviteMemberModal', () => {
  const mountComponent = () => {
    return mount(InviteMemberModal, {
      props: {
        modelValue: true
      },
      global: {
        stubs: {
          BaseModal: {
            template: '<div><slot /></div>'
          },

          BaseInput: {
            props: ['modelValue', 'label', 'placeholder'],
            emits: ['update:modelValue'],
            template: `
              <div>
                <label>{{ label }}</label>
                <input
                  :value="modelValue"
                  :placeholder="placeholder"
                  @input="$emit('update:modelValue', $event.target.value)"
                />
              </div>
            `
          },

          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },

          VSelect: {
            props: ['modelValue', 'items', 'label'],
            emits: ['update:modelValue'],
            template: `
              <select
                :value="modelValue"
                @change="$emit('update:modelValue', $event.target.value)"
              >
                <option
                  v-for="item in items"
                  :key="item"
                  :value="item"
                >
                  {{ item }}
                </option>
              </select>
            `
          }
        }
      }
    })
  }

  it('renders Invite Member title', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Invite Member')
  })

  it('renders username input', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('input').exists()).toBe(true)
    expect(wrapper.find('input').attributes('placeholder'))
      .toBe("Enter member's username")
  })

  it('renders role options', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Member')
    expect(wrapper.text()).toContain('Admin')
  })

  it('renders Cancel and Send Invite buttons', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Cancel')
    expect(wrapper.text()).toContain('Send Invite')
  })

  it('does not emit confirm when username is empty', async () => {
    const wrapper = mountComponent()

    await wrapper.get('button:last-of-type').trigger('click')

    expect(wrapper.emitted('confirm')).toBeUndefined()
  })

  it('emits confirm with username and role', async () => {
    const wrapper = mountComponent()

    const input = wrapper.find('input')

    await input.setValue('john123')

    await wrapper.get('button:last-of-type').trigger('click')

    expect(wrapper.emitted('confirm')).toEqual([
      [
        {
          username: 'john123',
          role: 'Member'
        }
      ]
    ])
  })

  it('trims whitespace from username', async () => {
    const wrapper = mountComponent()

    await wrapper.find('input').setValue('  john123  ')

    await wrapper.get('button:last-of-type').trigger('click')

    expect(wrapper.emitted('confirm')).toEqual([
      [
        {
          username: 'john123',
          role: 'Member'
        }
      ]
    ])
  })

  it('resets the form when Cancel is clicked', async () => {
    const wrapper = mountComponent()

    await wrapper.find('input').setValue('john123')

    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')

    expect(wrapper.find('input').element.value).toBe('')
  })
})