import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityCreateForm from '~/components/features/community/CommunityCreateForm.vue'

const createCommunity = vi.fn()

vi.mock('~/composables/useCommunity', () => ({
  useCommunity: () => ({
    createCommunity,
    error: null
  })
}))

vi.mock('~/composables/useSnackbar', () => ({
  useSnackBar: () => ({
    show: vi.fn()
  })
}))

describe('CommunityCreateForm', () => {
  const mountComponent = () => {
    return mount(CommunityCreateForm, {
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
          },
          'v-select': {
            template: '<select />'
          },
          'v-btn-toggle': {
            template: '<div><slot /></div>'
          },
          'v-btn': {
            template: '<button><slot /></button>'
          },
          'v-icon': {
            template: '<span />'
          }
        }
      }
    })
  }

  it('renders Create Community title', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Create Community')
  })

  it('renders community description field', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  it('renders visibility options', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Public')
    expect(wrapper.text()).toContain('Private')
  })

  it('renders Create button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Create')
  })

  it('renders Cancel button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Cancel')
  })
})