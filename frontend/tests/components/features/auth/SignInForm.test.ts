import { describe, it, vi, expect, beforeEach } from 'vitest'
import { mount} from '@vue/test-utils'
import AuthForm from '@/components/features/auth/SignInForm.vue'
import SignInForm from '@/components/features/auth/SignInForm.vue'

const push = vi.fn()
const loginMock = vi.fn()
const errorRef = ref('')

vi.mock('vue-router', () => ({
    useRouter: () => ({ push })
}))

vi.stubGlobal('useAuth', () => ({
    login: loginMock, 
    error: errorRef,
    loading: ref(false)
}))

describe('SignInForm.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        errorRef.value = ''
    })

    // Sucess Tests
    it('handles successful sign in and redirects to library', async () => {
        loginMock.mockResolvedValue(true)
        const wrapper = mount(SignInForm)

        const authForm = wrapper.findComponent({ name: 'AuthForm' })
        await authForm.vm.$emit('submit', { username: 'testUser', password: 'password@123'})

        expect(loginMock).toHaveBeenCalledWith({ username: 'testUser', password: 'password@123'})
        expect(push).toHaveBeenCalledWith('/library')
    })

    // Fail Tests
    it('sets error state when required fields are missing', async () => {
        const wrapper = mount(SignInForm)

        const authForm = wrapper.findComponent({ name: 'AuthForm' })
        await authForm.vm.$emit('submti', { username: '', password: ''})

        expect(errorRef.value).toBe('Please fill in all fields.')
        expect(loginMock).not.toHaveBeenCalled()
    })

    // Edge Case Tests
    it('does NOT navigate to /library if login returns false', async () => {
        loginMock.mockResolvedValue(false)
        const wrapper = mount(SignInForm)

        const authForm = wrapper.findComponent({ name: 'AuthForm' })
        await authForm.vm.$emit('submit', { username: 'baduser', password: 'wrongpassword' })

        expect(loginMock).toHaveBeenCalledWith({ username: 'baduser', password: 'wrongpassword'})
        expect(push).not.toHaveBeenCalled()
    })

    it('sets error state if only password is provided without username', async () => {
        const wrapper = mount(SignInForm)

        const authForm = wrapper.findComponent({ name: 'AuthForm' })
        await authForm.vm.$emit('submit', { username: '', password: 'somePassword' })

        expect(errorRef.value).toBe('Please fill in all fileds.')
        expect(loginMock).not.toHaveBeenCalled() 
    })
})