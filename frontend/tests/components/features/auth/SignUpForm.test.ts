import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SignUpForm from '~/components/features/auth/SignUpForm.vue'

const push = vi.fn()
const registerMock = vi.fn()
const errorRef = ref('')

vi.mock('vue-router', () => ({
    useRouter: () => ({ push })
}))

vi.stubGlobal('useAuth', () => ({
    register: registerMock,
    error: errorRef
}))

describe('SignUpForm.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        errorRef.value = ''
    })

    it('shows error if password and confirmPassword do not match', async () => {
        const wrapper = mount(SignUpForm)

        const authForm = wrapper.findComponent({ name: 'AuthForm' })
        await authForm.vm.$emit('submit', {
            firstName: 'John',
            lastName: 'Doe',
            username: 'johndoe',
            emailAddress: 'john@example.com',
            password: 'password@123',
            confirmPassword: 'password@123'
        })

        expect(registerMock).toHaveBeenCalledWith({
            firstName: 'John',
            lastName: 'Doe',
            username: 'johndoe',
            emailAddress: 'john@example.com',
            password: 'password@123',
        })
        expect(push).toHaveBeenCalledWith('/library')
    })
})