import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import SignUpPage from '~/pages/auth/signup.vue'

describe('SignUpPage', () => {
    const mountPage = () => {
        return mount(SignUpPage, {
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
                    SignUpForm: {
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

    it('renders the SignUPForm component inside the layout', () { 
        const wrapper = mountPage() 

        const form = wrapper.find('[data-test="sign-up-form"]')
        const col = wrapper.find('[data-test="v-col"]')

        expect(form.exists()).toBe(true)
        expect(col.find('[data-test="sign-up-form"]').exists()).toBe(true)
    })
})