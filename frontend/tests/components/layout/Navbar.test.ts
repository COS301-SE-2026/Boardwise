import { describe, it, expect, vi, beforeEach } from "vitest"
import { mount } from '@vue/test-utils'

import Navbar from "~/components/layout/Navbar.vue"

const mockDisplay = { lgAndUp: true}

vi.mock('vuetify', () => ({
    useDisplay: () => mockDisplay
}))

describe('Navbar.vue', () => {
    beforeEach(() => {
        mockDisplay.lgAndUp = true
    })

    const mountComponent = () => {
        return mount(Navbar, {
            global: {
                stubs: {
                    NuxtLink: {
                        template: '<a data-test="nuxt-link"><slot /></a>'
                    },
                    VAppBar: {
                        template: '<header><slot /></header>'
                    },
                    VAppBarNavIcon: {
                        template: '<button data-test="nav-icon" @click="$emit(\'click\')"></button>'
                    },
                    VTextField: true,
                    VIcon: true,
                    VMenu: {
                        template: '<div><slot name="activator" :props="{}" /><slot /></div>'
                    }, 
                    VCard: {
                        template: '<div><slot /></div>'
                    },
                    VNavigationDrawer: {
                        props: ['modelValue'],
                        template: '<aside v-if="modelValue" data-test="drawer"><slot /><slot name="append" /></aside>'
                    },
                    VList: {
                        template: '<ul><slot /></ul>'
                    },
                    VListItem: {
                        template: '<li></li>'
                    }
                }
            }
        })
    }

    it('renders desktop navigatin when lgAndUp is true', () => {
        const wrapper = mountComponent()

        expect(wrapper.find('.center').exists()).toBe(true) // Desktop search
        expect(wrapper.find('.right').exists()).toBe(true) // Desktop links
        expect(wrapper.find('[data-test="nav-icon"]').exists()).toBe(false) // No hamburger 
    })

    it('renders desktop navigatin when lgAndUp is false', () => {
        mockDisplay.lgAndUp = false
        const wrapper = mountComponent()

        expect(wrapper.find('.mobile').exists()).toBe(true)
        expect(wrapper.find('[data-test="nav-icon"]').exists()).toBe(true) // Desktop links
        expect(wrapper.find('.right').exists()).toBe(false) // Desktop links hidden
    })

    it('opens the navigation drawer when the hamburger icon is clicked', async () => {
        mockDisplay.lgAndUp = false
        const wrapper = mountComponent()

        expect(wrapper.find('[data-test="drawer"]').exists()).toBe(false)
        await wrapper.find('[data-test="nav-icon"]').trigger('click')

        expect(wrapper.find('[data-test="drawer]').exists()).toBe(true)
    })

    it('emits "ask-ai" when the Ask AI button is clicked on desktop', async () => {
        const wrapper = mountComponent()

        const aiButton = wrapper.find('[data-test="v-btn"]')
        await aiButton.trigger('click')

        expect(wrapper.emitted('ask-ai')).toBeTruthy()
    })
})