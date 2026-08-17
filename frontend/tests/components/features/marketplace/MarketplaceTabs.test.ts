import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarketplaceTabs from '~/components/features/marketplace/MarketplaceTabs.vue'

describe('MarketplaceTabs.vue', () => {
    it('renders marketplace tabs', () => {
        const wrapper = mount(MarketplaceTabs, {
            props: {
                modelValue: 'Community Listings'
            }
        })

        const tabs = wrapper.find('[data-test="marketplace-tabs"]')

        expect(tabs.exists()).toBe(true)
        expect(wrapper.text()).toContain('Community Listings')
        expect(wrapper.text()).toContain('Web')
    })

    it('uses the community listings tab by default', () => {
        const wrapper = mount(MarketplaceTabs)

        expect(wrapper.props('modelValue')).toBe('Community Listings')
    })

    it('emits update:modelValue when the selected tab changes', async () => {
        const wrapper = mount(MarketplaceTabs, {
            global: {
                stubs: {
                    VTabs: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<div><slot /></div>'
                    }
                }
            }
        })

        const tabs = wrapper.findComponent({ name: 'VTabs' })

        await tabs.vm.$emit('update:modelValue', 'Web')

        expect(wrapper.emitted('update:modelValue')).toBeTruthy()
        expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['Web'])
    })
})