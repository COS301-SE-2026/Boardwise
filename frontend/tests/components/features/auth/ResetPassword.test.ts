import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'

import { useAuth } from '~/composables/useAuth'
import ResetPassword from '~/components/features/auth/ResetPassword.vue'

vi.mock('~/composables/useAuth', () => ({
    useAuth: vi.fn()
}))

const mockPush = vi.fn()
const mockRoute = { query: { token: 'valid-token-123' } }

vi.mock('vue-router', () => ({
    useRouter: () => ({ push: mockPush }),
    useRoute: () => mockRoute
}))

describe('ResetPassword.vue', () => {
    const mockResetPassword = vi.fn()
    const mockError = ref('')

    beforeEach(() => {
        vi.clearAllMocks()
        mockError.value = ''
        mockRoute.query.token = 'valid-token-123'

        vi.mocked(useAuth).mockReturnValue({
            forgotPassword: mockResetPassword,
            error: mockError
        } as any)
    })

    const mountComponent = (formPayload = { password: 'newpass', confirmPassword: 'newpass'}) => {
        return mount(ResetPassword, {
            global: {
                stubs: {
                    AuthForm: {
                        template: '<form data-test="auth-form" @submit.prevent="$emit(\'submit\', payload)"></form>',
                        data() { return { payload: formPayload } }
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
    }

    it('renders the AuthForm if a token is present in the route query', () => {
        const wrapper = mountComponent()
        expect(wrapper.find('[data-test="auth-form"]').exists()).toBe(true)
    })

    it('shows an error and hides the form if no token is present', async () => {
        const mockRoute = { query: { token: 'valid-token-123' } }
        const wrapper = mountComponent()

        expect(wrapper.find('[data-test="auth-form"]').exists()).toBe(false)
        expect(wrapper.text()).toContain('invalid or has expired')
    })

    it('sets a local error if passwords do not match', async () => {
        const wrapper = mountComponent({ password: 'pass', confirmPassword: 'differentpass'})

        await wrapper.find('[data-test="auth-form"]').trigger('submit')

        expect(mockResetPassword).not.toHaveBeenCalled()
        expect(wrapper.find('[data-test="v-alert"]').text()).toContain('Passwords do not match.')
    })

    it('calls resetPassword and redirects to signin on success', async () => {
        mockResetPassword.mockResolvedValue(true)
        const wrapper = mountComponent({ password: 'newpass', confirmPassword: 'newpass' })

        await wrapper.find('[data-test="auth-form"]').trigger('submit')

        expect(mockResetPassword).toHaveBeenCalledWith({
            token: 'valid-token-123',
            password: 'newpass'
        })

        expect(mockPush).toHaveBeenCalledWith('/auth/signin')
    })  
})