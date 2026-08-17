import { describe, it, expect } from 'vitest'
import { mount} from '@vue/test-utils'
import AuthForm from '@/components/features/auth/AuthForm.vue'
import { userInfo } from 'node:os'

describe('AuthForm.vue', () => {
    const sampleFields = [
        { key: 'username', placeholder: 'Username', type: 'text' },
        { key: 'password', placeholder: 'Password', type: 'password' }
    ]

    it('renders title and input field dynamically', () => {
        const wrapper = mount(AuthForm, {
            props: {
                title: 'Sign In',
                buttonText: 'Submit',
                fields: sampleFields
            }
        })

        expect(wrapper.find('[data-test="auth-title"]').text()).toBe('Sign In')
        expect(wrapper.find('[data-test="input-username"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-password"]').exists()).toBe(true)
    })

    it('emits submit event with reactive form values when a button is clicked' , async() => {
        const wrapper = mount(AuthForm, {
            props: {
                title: 'Sign In',
                buttonText: 'Submit',
                fields: sampleFields
            }
        })

        const button = wrapper.find('[data-test="submit-button"]')
        await button.trigger('click')

        expect(wrapper.emitted('submit')).toBeTruthy()
        expect(wrapper.emitted('submit')?.[0]).toEqual([{ username: '', password: ''}])
    })
})