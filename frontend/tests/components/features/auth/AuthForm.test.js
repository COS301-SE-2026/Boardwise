import { mount } from '@vue/test-utils'
import AuthForm from '../components/features/auth/AuthForm.vue'

describe('AuthForm', () => {

  it('emits submit event', async () => {

    const wrapper = mount(AuthForm, {
      props: {
        title: 'Sign In',
        buttonText: 'Sign In'
      }
    })

    const button = wrapper.find('button')

    await button.trigger('click')

    expect(wrapper.emitted()).toHaveProperty('submit')

  })

})