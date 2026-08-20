import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatComposer from '~/components/features/chat/ChatComposer.vue'

describe('ChatComposer', () => {
    it('renders the message input and send button', () => {
        const wrapper = mount(ChatComposer, {
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    BaseInput: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                            <input
                                :value="modelValue"
                                @input="$emit('update:modelValue', $event.target.value)"
                            />
                        `
                    },
                    BaseButton: {
                        props: ['disabled'],
                        template: '<button :disabled="disabled"><slot /></button>'
                    }
                }
            }
        })

        expect(wrapper.text()).toContain('Send')
        expect(wrapper.find('input').exists()).toBe(true)
    })

    it('emits the trimmed message when send is clicked', async () => {
        const wrapper = mount(ChatComposer, {
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    BaseInput: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                            <input
                                :value="modelValue"
                                @input="$emit('update:modelValue', $event.target.value)"
                            />
                        `
                    },
                    BaseButton: {
                        props: ['disabled'],
                        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
                    }
                }
            }
        })

        const input = wrapper.find('input')

        await input.setValue('  Hello world  ')
        await wrapper.find('button').trigger('click')

        expect(wrapper.emitted('send')).toEqual([['Hello world']])
    })

    it('does not emit when the message is empty', async () => {
        const wrapper = mount(ChatComposer, {
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    BaseInput: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                            <input
                                :value="modelValue"
                                @input="$emit('update:modelValue', $event.target.value)"
                            />
                        `
                    },
                    BaseButton: {
                        props: ['disabled'],
                        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
                    }
                }
            }
        })

        await wrapper.find('button').trigger('click')

        expect(wrapper.emitted('send')).toBeUndefined()
    })

    it('clears the input after sending', async () => {
        const wrapper = mount(ChatComposer, {
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    BaseInput: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                            <input
                                :value="modelValue"
                                @input="$emit('update:modelValue', $event.target.value)"
                            />
                        `
                    },
                    BaseButton: {
                        props: ['disabled'],
                        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
                    }
                }
            }
        })

        const input = wrapper.find('input')

        await input.setValue('Hello')
        await wrapper.find('button').trigger('click')

        expect((input.element as HTMLInputElement).value).toBe('')
    })
})