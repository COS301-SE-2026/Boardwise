import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import ForgotPasswordPage from '~/pages/auth/forgotpassword.vue'
import ResetPasswordPage from '~/pages/auth/resetpassword.vue'

const globalStubs = {
    VConatiner: {
        template: '<div><slot /></div>' 
    },
    VRow: {
        template: '<div><slot /></div>'
    },
    VCol: {
        template: '<div><slot /></div>'
    }
}

describe('Forgot Password Page', () => {
    it('renders the ForgotPassword component within the layout', () => {
        const wrapper = mount(ForgotPasswordPage, {
            global: {
                stubs: {
                    ...globalStubs,
                    ForgotPassword: {
                        template: '<div data-test="forgot-password-form"></div>'
                    }
                }
            }
        })
    })
})

describe('Reset Password Page', () => {
    it('renders the ResetPassword component within the layout', () => {
        const wrapper = mount(ResetPasswordPage, {
            global: {
                stubs: {
                    ...globalStubs,
                    ResetPassword: {
                        template: '<div data-test="reset-password-form></div>'
                    }
                }
            }
        })

        expect(wrapper.find('[data-test="reset-password-form"]').exists()).toBe(true)
    })
})