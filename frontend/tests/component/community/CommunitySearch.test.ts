import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ExploreSearch from '~/components/features/community/ExploreSearch.vue'

describe('ExploreSearch', () => {
  const mountComponent = () => {
    return mount(ExploreSearch, {
      global: {
        stubs: {
          BaseSearch: {
            props: ['modelValue'],
            template: `
              <input
                :value="modelValue"
                placeholder="Search for a community..."
              />
            `
          },
          BaseButton: {
            template: `
              <button @click="$emit('click')">
                <slot />
              </button>
            `
          }
        }
      }
    })
  }

  it('renders the search input', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('input').exists()).toBe(true)
  })

  it('renders the create community button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Create a community')
  })

  it('emits create-community when the button is clicked', async () => {
    const wrapper = mountComponent()

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('create-community')).toBeTruthy()
  })
})