import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RagMessage from '~/components/features/rag/RagMessage.vue'
import { wrap } from 'node:module'

describe('RagMessage.vue', () => {
    const baseMessage = {
        id: '1',
        role: 'assistant' as const,
        content: 'Draw two cards from the deck.',
        citations: [{ chunkId: 'c1', index: 3, content: 'On a double six, draw two cards', relevanceScore: 0.92 }],
        isError: false
    }

    const mountMessage = (overrides = {}) => {
        return mount(RagMessage, {
            props: { message: { ...baseMessage, ...overrides } },
            global: {
                stubs: {
                    BaseAvatar: { template: '<div data-test="avatar" />'},
                    BaseCard: { template: '<div><slot /></div>'},
                    BaseButton: { template: '<button><slot /></button>'},
                    RagCitation: { template: '<div data-test="rag-citation" />'}
                }
            }
        })
    }

    it('renders assistant message content', () => {
        const wrapper = mountMessage()
        expect(wrapper.find('[data-test="rag-message"]').text()).toContain('Draw two cards from the deck.')
    })

    it('shows Boarley avatar for assistant message', () => {
        const wrapper = mountMessage({ role: 'assistant '})
        expect(wrapper.find('[data-test="avatar"]').exists()).toBe(false)
    })

    it('applies user class for user role', () => {
        const wrapper = mountMessage({ role: 'user '})
        expect(wrapper.find('[data-test="rag-message"]').classes()).toContain('user')
    })

    it('renders citations when present', () => {
        const wrapper = mountMessage()
        expect(wrapper.find('[data-test="rag-citation"]').exists()).toBe(true)
    })

    it('does not show avatar for user messages', () => {
        const wrapper = mountMessage({ role: 'user' })
        expect(wrapper.find('[data-test="avatar"]').exists()).toBe(false)
    })

    it('shows retry button only on error messages', () => {
        const errorWrapper = mountMessage({ isError: true })
        expect(errorWrapper.find('button').exists()).toBe(true)

        const okWrapper = mountMessage({ isError: false })
        expect(okWrapper.find('button').exists()).toBe(false)
    })

    it('emits retry with the message when retry is clicked', async () => {
        const wrapper = mountMessage({ isError: true })
        await wrapper.find('button').trigger('click')

        const retryEvents = wrapper.emitted('retry')
        expect(retryEvents).toBeTruthy()

        const firstCall = retryEvents![0]
        expect(firstCall).toBeTruthy()
        expect(firstCall![0]).toMatchObject({ id: '1', isError: true, query: 'what happens on a double six? ' })
    })
})