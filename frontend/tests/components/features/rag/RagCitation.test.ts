import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RagCitation from '~/components/features/rag/RagCitation.vue'

describe('RagCitation.vue', () => {
    const citation = {
        chunkId: 'c1',
        index: 3,
        content: 'On a double six, draw two cards.',
        relevanceScore: 0.92
    }

    const mountCitation = (props = {}) => {
        return mount(RagCitation, {
            props: { citation, ...props },
            global: {
                stubs: {
                    VIcon: { template: '<i />'}
                }
            }
        })
    }

    it('renders the citation chunk index', () => {
        const wrapper = mountCitation()
        expect(wrapper.find('[data-test="rag-citation"]').text()).toContain('chunk 3')
    })

    it('renders a different index correctly', () => {
        const wrapper = mountCitation({ citation: { ...citation, index: 7}})
        expect(wrapper.find('[data-test="rag-citation"]').text()).toContain('chunk 7')
    })

    it('does not render the raw chunk index as the visible text', () => {
        const wrapper = mountCitation()
        expect(wrapper.find('data-test="rag-citation-text"]').text()).not.toBe('3')
    })
})