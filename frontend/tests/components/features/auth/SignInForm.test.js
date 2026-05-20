import { mount } from '@vue/test-utils'
import LoginForm from '../components/features/auth/LoginForm.vue'

describe('LoginForm', () => {

  it('renders login form', () => {

    const wrapper = mount(LoginForm)

    expect(wrapper.text()).toContain('Login')

  })

})