import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AddCommunityModal from '~/components/features/community/AddCommunityModal.vue'

describe('AddCommunityModal', () => {
  const mountComponent = () => {
    return mount(AddCommunityModal, {
      props: {
        modelValue: true
      },
      global: {
        stubs: {
          BaseModal: {
            props: ['modelValue'],
            template: '<div><slot /></div>'
          },
          BaseInput: {
            props: ['modelValue', 'label'],
            template: '<input :value="modelValue" :aria-label="label" />'
          },
          BaseTextArea: {
            props: ['modelValue'],
            template: '<textarea :value="modelValue"></textarea>'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          'v-select': {
            template: '<select></select>'
          },
          'v-btn-toggle': {
            template: '<div><slot /></div>'
          },
          'v-btn': {
            template: '<button><slot /></button>'
          }
        }
      }
    })
  }

  it('renders Add Community title', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Add Community')
  })

  it('renders community name input', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('input').exists()).toBe(true)
  })

  it('renders description field', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  it('renders Public and Private options', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Public')
    expect(wrapper.text()).toContain('Private')
  })

  it('renders upload image option', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Upload Image')
  })

  it('renders Cancel and Add Community buttons', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Cancel')
    expect(wrapper.text()).toContain('Add Community')
  })
})