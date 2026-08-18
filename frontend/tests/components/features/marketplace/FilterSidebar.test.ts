import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FilterSidebar from '~/components/features/marketplace/FilterSidebar.vue'

describe('FilterSidebar.vue', () => {

    const mountFilterSidebar = () => {
        return mount(FilterSidebar, {
            global: {
                stubs: {
                    BaseFilterSidebar: {
                        emits: ['reset'],
                        template: `
                            <div data-test="filter-sidebar">
                                <button
                                    data-test="reset-button"
                                    @click="$emit('reset')"
                                >
                                    Reset
                                </button>
                                <slot />
                            </div>
                        `
                    },

                    BaseFilterGroup: {
                        props: ['title'],
                        template: `
                            <div>
                                <h3>{{ title }}</h3>
                                <slot />
                            </div>
                        `
                    },

                    VCheckbox: {
                        props: [
                            'modelValue',
                            'value',
                            'label'
                        ],
                        emits: ['update:modelValue'],
                        template: `
                            <input
                                type="checkbox"
                                :data-test="$attrs['data-test']"
                                :checked="modelValue"
                                @change="
                                    $emit(
                                        'update:modelValue',
                                        value ?? $event.target.checked
                                    )
                                "
                            />
                        `
                    },

                    VTextField: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                            <input
                                type="number"
                                :data-test="$attrs['data-test']"
                                :value="modelValue"
                                @input="
                                    $emit(
                                        'update:modelValue',
                                        $event.target.value
                                    )
                                "
                            />
                        `
                    }
                }
            }
        })
    }

    it('renders the marketplace filter options', () => {
        const wrapper = mountFilterSidebar()

        expect(wrapper.text()).toContain('Strategy')
        expect(wrapper.text()).toContain('Family')
        expect(wrapper.text()).toContain('Party')
        expect(wrapper.text()).toContain('Card')
        expect(wrapper.text()).toContain('Abstract')

        expect(wrapper.find('[data-test="rent-filter"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="sale-filter"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="min-price"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="max-price"]').exists()).toBe(true)
    })

    it('emits filters when a genre is selected', async () => {
        const wrapper = mountFilterSidebar()

        await wrapper.find('[data-test="genre-strategy"]').trigger('click')
        const emitted = wrapper.emitted('filter')

        expect(emitted).toBeTruthy()

        expect(emitted?.[0]).toEqual([
            {
                genres: ['strategy'],
                conditions: [],
                rent: false,
                sale: false,
                minPrice: null,
                maxPrice: null
            }
        ])
    })

    it('emits reset filter values when reset is triggered', async () => {
        const wrapper = mountFilterSidebar()

        await wrapper.find('[data-test="genre-strategy"]').trigger('click')
        await wrapper.find('[data-test="reset-button"]').trigger('click')
        const emitted = wrapper.emitted('filter')

        expect(emitted).toBeTruthy()

        const lastEmission = emitted?.[emitted.length - 1]

        expect(lastEmission).toEqual([
            {
                genres: null,
                conditions: [],
                rent: false,
                sale: false,
                minPrice: null,
                maxPrice: null
            }
        ])
    })

    it('renders all condition options', () => {
        const wrapper = mountFilterSidebar()

        expect(wrapper.find('[data-test="condition-new"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="condition-like-new"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="condition-good"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="condition-fair"]').exists()).toBe(true)
    })

})