import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RagPanel from '~/components/features/rag/RagPanel.vue'
import RagComposer from '~/components/features/rag/RagComposer.vue'

const askQuestion = vi.fn()
const clearConversation = vi.fn()

vi.mock('~/composables/useRag.ts', () => ({
    useRag: () => ({
        messages: [], 
        isLoading: false,
        askQuestion,
        clearConversation
    })
}))

describe('RagPanel.vue', () => {
    beforeEach(() => {
        askQuestion.mockClear()
        clearConversation.mockClear()
    })

    const mountPanel = (props = {}) => {
        return mount(RagPanel, {
            props: { 
                modelValue: true,
                rulebook: { id: 'rb1', title: 'Catan'},
                ...props 
            },
            global: {
                stubs: {
                    RagFeed: {
                        template: '<div data-test="rag-feed" />',
                        emits: ['retry']
                    },
                    RagComposer: {
                        template: '<div data-test="rag-composer" />',
                        emits: ['send']
                    }
                }
            }
            
        })
    }

    it('does not render when modelValue is false', () => {
        const wrapper = mountPanel({ modelValue: false })
        expect(wrapper.find('[data-test="rag-panel"]').exists()).toBe(false)
    })

    it('shows the rulebook title in the header', () => {
        const wrapper = mountPanel()
        expect(wrapper.text()).toContain('Catan')
    })

    it('emits update:modelValue false when close is clicked', async () => {
        const wrapper = mountPanel()
        await wrapper.find('button').trigger('click')

        const closeEvents = wrapper.emitted('updateP:modelValue')
        expect(closeEvents).toBeTruthy()

        const firstCall = closeEvents![0]
        expect(firstCall).toBeTruthy()
        expect(firstCall![0]).toBe(false)
    })

    it('calls askQuestion with rulebook id and query on send',async () => {
        const wrapper = mountPanel()
        await wrapper.findComponent({ name: 'RagComposer'}).vm.$emit('send', 'how many players?')
        expect(askQuestion).toHaveBeenCalledWith('rb1', 'how many players?')
    })

    it('does not call askQuestion if no rulebook is selected',async () => {
        const wrapper = mountPanel({ rulebook: null })
        await wrapper.findComponent({ name: 'RagComposer'}).vm.$emit('send', 'test')
        expect(askQuestion).not.toHaveBeenCalled()
    })

    it('calls askQuestion with the original query on retry ', async () => {
        const wrapper = mountPanel()
        await wrapper.findComponent({ name: 'RagFeed' }).vm.$emit('retry', {
            id: '1',
            role: 'assistant',
            content: 'Failed to get an answer',
            isError: true,
            query: 'how many players?'
        })

        expect(askQuestion).toHaveBeenCalledWith('rb1', 'how many players?')
    })

    it('does not call askQuestion on retry if the message has no query ', async () => {
        const wrapper = mountPanel()
        await wrapper.findComponent({ name: 'RagFeed' }).vm.$emit('retry', {
            id: '1',
            role: 'assistant',
            content: 'Failed to get an answer',
            isError: true,
        })

        expect(askQuestion).not.toHaveBeenCalled()
    })

    it('clears conversation when rulebook changes',async () => {
        const wrapper = mountPanel({ id: 'rb1', title: 'Catan' })
        await wrapper.setProps({ rulebook: { id: 'rb2', title: 'Wingspan'}})
        expect(clearConversation).toHaveBeenCalled()
    })
})