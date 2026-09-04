import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SignInPage from '~/pages/auth/signin.vue'

describe('SignInPage', () => {
    const mountPage = () => {
        return mount(SignInPage, {
            global: {
                stubs: {
                    VContainer: {
                        template: '<div data-test="v-container"><slot /></div>'
                    },
                    VRow: {
                        template: '<div data-test="v-row"><slot /></div>'
                    }, 
                    VCol: {
                        template: '<div data-test="v-col"><slot /></div>'
                    },
                    SignInForm: {
                        template: '<div data-test="sign-in-form"></div>'
                    }
                }
            }
        })
    }

    it('renders the layout containers properly', () => {
        const wrapper = mountPage() 

        expect(wrapper.find('[data-test="v-container"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="v-row"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="v-col"]').exists()).toBe(true)
    })

    it('renders the SignInForm component inside the layout', () => {
        const wrapper = mountPage()

        const form = wrapper.find('[data-test="sign-in-form"')
        const col = wrapper.find('[data-test="v-row"]')

        expect(form.exists()).toBe(true)
        expect(col.find('[data-test="sign-in-form"]').exists()).toBe(true)
    })
})