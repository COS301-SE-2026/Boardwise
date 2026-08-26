import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'

import { useAuth } from '~/composables/useAuth'
import ForgotPassword from '~/components/features/auth/ForgotPassword.vue'

vi.mock('~/composables/useAuth', () => ({
    useAuth: vi.fn()
}))

describe('ForgotPassword.vue', () => {
    const mockForgotPassword = vi.fn()
    const mockError = ref('')

    beforeEach(() => {
        vi.clearAllMocks()
        mockError.value = ''
        vi.mocked(useAuth).mockReturnValue({
            forgotPassword: mockForgotPassword,
            error: mockError
        } as any) 
    })

    const mountComponent = () => {
        return mount(ForgotPassword, {
            global: {
                stubs: {
                    AuthForm: {
                        template: '<form data-test="auth-form" @submit.prevent="$emit(\'submit\', { emailAddress: \'test@test.com\' })"></form>'
                    },
                    BaseCard: {
                        template: '<div data-test="reset-sent-card"></div>'
                    },
                    VContainer: {
                        template: '<div><slot /></div>'
                    },
                    VAlert: {
                        template: '<div data-test="v-alert"><slot /></div>'
                    },
                    VIcon: true,
                    NuxtLink: true
                }
            }
        })
    }

    it('renders the AuthForm by default', () => {
        const wrapper = mountComponent()
        expect(wrapper.find('[data-test="auth-form"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="reset-sent-card"]').exists()).toBe(true)
    })

    it('displays an error if submission lacks an email address', async () => {
        const wrapper = mount(ForgotPassword, {
            global: {
                stubs: {
                    AuthForm: {
                        template: '<form data-test="auth-form" @submit.prevent="$emit(\'submit\', { emailAddress: \'\' })"><form>'
                    },
                    VContainer: {
                        template: '<div><slot /></div>'
                    },
                    VAlert: {
                        template: '<div data-test="v-alert"><slot /></div>'
                    },
                    NuxtLink: true
                }
            }
        })

        await wrapper.find('[data-test="auth-form"]').trigger('submit')

        expect(mockError.value).toBe('Please enter your email address.')
    })

    it('calls forgotPassword and switches to success card on valid submit', async () => {
        mockForgotPassword.mockResolvedValue(true)
        const wrapper = mountComponent()

        await wrapper.find('[data-test="auth-form"]').trigger('submit')

        expect(mockForgotPassword).toHaveBeenCalledWith('test@test.com')
        await wrapper.vm.$nextTick()

        expect(wrapper.find('[data-test="auth-form"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="reset-sent-card"]').exists()).toBe(true)
        expect(wrapper.text()).toContain('test@test.com')
    })
    
})