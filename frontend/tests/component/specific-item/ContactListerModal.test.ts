import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

import ContactListerModal from '~/components/features/specific-item/ContactListerModal.vue'

describe('ContactListerModal', () => {
  const mountComponent = (props = {}) =>
    mount(ContactListerModal, {
      props: {
        modelValue: true,
        listingTitle: 'Catan Board Game',
        ...props
      },
      global: {
        stubs: {
          BaseModal: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template: `
              <div data-test="modal">
                <slot />
              </div>
            `
          },

          BaseButton: {
            props: ['variant'],
            template: `
              <button
                :data-variant="variant"
                @click="$emit('click')"
              >
                <slot />
              </button>
            `
          },

          'v-text-field': {
            props: ['modelValue', 'label', 'placeholder', 'type'],
            emits: ['update:modelValue'],
            template: `
              <input
                :aria-label="label"
                :placeholder="placeholder"
                :type="type || 'text'"
                :value="modelValue"
                @input="$emit('update:modelValue', $event.target.value)"
              />
            `
          },

          'v-textarea': {
            props: ['modelValue', 'label', 'placeholder'],
            emits: ['update:modelValue'],
            template: `
              <textarea
                :aria-label="label"
                :placeholder="placeholder"
                :value="modelValue"
                @input="$emit('update:modelValue', $event.target.value)"
              />
            `
          },

          'v-snackbar': {
            template: '<div data-test="snackbar"><slot /></div>'
          }
        }
      }
    })

  it('renders the contact form', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Contact lister')
    expect(wrapper.find('[aria-label="Your name"]').exists()).toBe(true)
    expect(wrapper.find('[aria-label="Your email"]').exists()).toBe(true)
    expect(wrapper.find('[aria-label="Message"]').exists()).toBe(true)
  })

  it('uses the listing title in the message placeholder', () => {
    const wrapper = mountComponent({
      listingTitle: 'Ticket to Ride'
    })

    expect(
      wrapper.find('[aria-label="Message"]').attributes('placeholder')
    ).toContain('Ticket to Ride')
  })

  it('does not send when name is empty', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[aria-label="Message"]').setValue('I am interested in this item.')

    const sendButton = wrapper
      .findAll('button')
      .find(button => button.text() === 'Send message')

    await sendButton!.trigger('click')

    expect(wrapper.emitted('sent')).toBeUndefined()
  })

  it('does not send when message is empty', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[aria-label="Your name"]').setValue('Lesa Nkosi')

    const sendButton = wrapper
      .findAll('button')
      .find(button => button.text() === 'Send message')

    await sendButton!.trigger('click')

    expect(wrapper.emitted('sent')).toBeUndefined()
  })

  it('emits the message when valid details are provided', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[aria-label="Your name"]').setValue('Lesa Nkosi')
    await wrapper.find('[aria-label="Your email"]').setValue('lesa@example.com')
    await wrapper.find('[aria-label="Message"]').setValue(
      'I am interested in this listing.'
    )

    const sendButton = wrapper
      .findAll('button')
      .find(button => button.text() === 'Send message')

    await sendButton!.trigger('click')

    expect(wrapper.emitted('sent')).toEqual([
      [
        {
          name: 'Lesa Nkosi',
          email: 'lesa@example.com',
          message: 'I am interested in this listing.'
        }
      ]
    ])
  })

  it('clears the form after sending', async () => {
    const wrapper = mountComponent()

    const name = wrapper.find('[aria-label="Your name"]')
    const email = wrapper.find('[aria-label="Your email"]')
    const message = wrapper.find('[aria-label="Message"]')

    await name.setValue('Lesa Nkosi')
    await email.setValue('lesa@example.com')
    await message.setValue('I am interested.')

    const sendButton = wrapper
      .findAll('button')
      .find(button => button.text() === 'Send message')

    await sendButton!.trigger('click')

    expect(name.element.value).toBe('')
    expect(email.element.value).toBe('')
    expect(message.element.value).toBe('')
  })
})