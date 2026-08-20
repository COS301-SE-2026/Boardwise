import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityEditModal from '~/components/features/community/CommunityEditModal.vue'

const editCommunity = vi.fn()

vi.mock('~/composables/useCommunity', () => ({
  useCommunity: () => ({
    editCommunity,
    error: null
  })
}))

vi.mock('~/composables/useSnackbar', () => ({
  useSnackBar: () => ({
    show: vi.fn()
  })
}))

describe('CommunityEditModal', () => {
  const community = {
    id: '1',
    name: 'Board Games',
    description: 'A board game community',
    visibility: 'Public',
    image: 'board-games.jpg'
  }

  const mountComponent = () => {
    return mount(CommunityEditModal, {
      props: {
        modelValue: true,
        community
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

  it('renders Edit Community title', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Edit Community')
  })

  it('renders Save Changes button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Save Changes')
  })

  it('renders Cancel button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Cancel')
  })

  it('renders Public and Private options', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Public')
    expect(wrapper.text()).toContain('Private')
  })

  it('loads the community information', () => {
    const wrapper = mountComponent()

    expect(wrapper.vm.form.name).toBe('Board Games')
    expect(wrapper.vm.form.description).toBe('A board game community')
    expect(wrapper.vm.form.visibility).toBe('Public')
  })
})