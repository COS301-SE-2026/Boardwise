import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CreateEventModal from '~/components/features/community/CreateEventModal.vue'

describe('CreateEventModal', () => {
  const mountComponent = () => {
    return mount(CreateEventModal, {
      props: {
        modelValue: true
      },
      global: {
        stubs: {
          BaseModal: {
            template: '<div><slot /></div>'
          },
          BaseInput: {
            template: '<input />'
          },
          BaseTextArea: {
            template: '<textarea />'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })
  }

  it('renders Create Event title', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Create Event')
  })

  it('renders event fields', () => {
    const wrapper = mountComponent()

    expect(wrapper.findAll('input').length).toBe(5)
  })

  it('renders description field', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  it('renders Cancel button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Cancel')
  })

  it('renders Create Event button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Create Event')
  })

  it('emits create when Create Event is clicked', async () => {
    const wrapper = mountComponent()

    const buttons = wrapper.findAll('button')

    await buttons[1].trigger('click')

    expect(wrapper.emitted('create')).toBeTruthy()
  })
})