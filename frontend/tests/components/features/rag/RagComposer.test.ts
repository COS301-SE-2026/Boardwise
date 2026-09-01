import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RagComposer from '~/components/features/rag/RagComposer.vue'

describe('RagComposer.vue', () => {
    const mountComposer = (props = {}) => {
        return mount(RagComposer, {
            props,
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>'},
                    BaseInput: { 
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" @keyup.enter="$emit(\'keyup\', $event)" />',
                        props: ['modelValue']
                    }, 
                    BaseButton: { 
                        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'},
                        props: ['disabled']
                }
            }
        })
    }

    it('does not emit send when input is empty', async () => {
        const wrapper = mountComposer()
        await wrapper.find('button').trigger('click')
        expect(wrapper.emitted('send')).toBeFalsy()
    })

    it('emits send with the message when retry is clicked', async () => {
        const wrapper = mountComposer()
        const input = wrapper.find('input')

        await input.setValue(' what happens on a double six? ')
        await wrapper.find('button').trigger('click')
        
        const sendEvents = wrapper.emitted('send')
        expect(sendEvents).toBeTruthy()

        const firstCall = sendEvents![0]
        expect(firstCall).toBeTruthy()
        expect(firstCall![0]).toBe('what happens on double six?')
    })

    it('disables send button while loading', () => {
        const wrapper = mountComposer({ isLoading: true })
        expect(wrapper.find('button').attributes('disabled')).toBeDefined()
    })

    it('disables send button when input is empty', () => {
        const wrapper = mountComposer()
        expect(wrapper.find('button').attributes('disabled')).toBeDefined()
    })

    it('disables the input while loading', () => {
        const wrapper = mountComposer({ isLoading: true })
        expect(wrapper.find('input').attributes('disabled')).toBeDefined()
    })
})