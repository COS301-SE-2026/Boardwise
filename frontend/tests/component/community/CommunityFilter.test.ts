import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityFilter from '~/components/features/community/CommunityFilter.vue'

describe('CommunityFilter', () => {
  const mountComponent = () => {
    return mount(CommunityFilter, {
      global: {
        stubs: {
          BaseFilterSidebar: {
            emits: ['reset'],
            template: '<div class="filter-sidebar"><slot /></div>'
          },
          BaseFilterGroup: {
            props: ['title'],
            template: `
              <div class="filter-group">
                <h3>{{ title }}</h3>
                <slot />
              </div>
            `
          },
          'v-checkbox': {
            props: ['label', 'value'],
            emits: ['update:modelValue'],
            template: `
              <label>
                <input type="checkbox" />
                {{ label }}
              </label>
            `
          }
        }
      }
    })
  }

  it('renders the visibility filter', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Visibility')
    expect(wrapper.text()).toContain('Public')
    expect(wrapper.text()).toContain('Private')
  })

  it('renders the category filter', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Category')
    expect(wrapper.text()).toContain('Strategy')
    expect(wrapper.text()).toContain('Family')
  })

  it('emits filter data when selections change', async () => {
    const wrapper = mountComponent()

    const vm = wrapper.vm as any

    vm.selectedTypes = ['public']

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('filter')).toBeTruthy()
  })
})