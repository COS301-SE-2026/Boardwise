import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RagFeed from '~/components/features/rag/RagFeed.vue'


describe('RagFeed.vue', () => {
    const messages = [
        { id: '1', role: 'user' as const, content: 'What happens on a double six?', query: 'What happens on a double six?'},
        { id: '2', role: 'assistant' as const, content: 'You get an extra turn.', query: 'What happens on a double six?'}
    ]

    const mountFeed = (props = {}) => {
        return mount(RagFeed, {
            props: { messages: [], ...props } ,
            global: {
                stubs: {
                    RagMessage: {
                        template: '<div data-test="rag-message" @click="$emit(\'retry\', message)" />',
                        props: ['message']
                    }
                }
            }
            
        })
    }

    it('shows empty state when there are no messaged and not loading', () => {
        const wrapper = mountFeed({ messages: [] })
        expect(wrapper.text()).toContain('Ask a question about this rulebook')
    })

    it('renders a message for each item', () => {
        const wrapper = mountFeed({ messages })
        expect(wrapper.findAll('[data-test="rag-message"]')).toHaveLength(2)
    })

    it('shows loading indicator when isLoading is true', () => {
        const wrapper = mountFeed({ messages, isLoading: true })
        expect(wrapper.find('[data-test="v-progress-circular"]').exists()).toBe(true)
    })

    it('does not show empty state while loading with no messages', () => {
        const wrapper = mountFeed({ messages: [], isLoading: true })
        expect(wrapper.text()).not.toContain('Ask a question about this rulebook')
    })

    it('shows no-result state when hasNoResultis true and messages exist', () => {
        const wrapper = mountFeed({ messages, hasNoResult: true })
        expect(wrapper.find('[data-test="reg-no-result"]').exists()).toBe(true)
    })

    it('forwards retry event from a message ', async () => {
        const wrapper = mountFeed({ messages })
        await wrapper.findComponent({ name: 'RagMessage' }).vm.$emit('retry', messages[1])

        const retryEvents = wrapper.emitted('retry')
        expect(retryEvents).toBeTruthy()

        const firstCall = retryEvents![0]
        expect(firstCall).toBeTruthy()
        expect(firstCall![0]).toMatchObject({ id: '1'})
    })
})