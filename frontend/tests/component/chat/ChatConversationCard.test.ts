import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatConversationCard from '~/components/features/chat/ChatConversationCard.vue'

const conversation = {
    id: '1',
    name: 'John Doe',
    avatar: '/avatar.jpg',
    time: '10:30',
    lastMessage: 'Hello there',
    online: true,
    unread: 3
}

describe('ChatConversationCard', () => {
    const createWrapper = (active = false) =>
        mount(ChatConversationCard, {
            props: {
                conversation,
                active
            },
            global: {
                stubs: {
                    BaseCard: {
                        template: '<div><slot /></div>'
                    },
                    BaseAvatar: {
                        props: ['src', 'name', 'size'],
                        template: '<div class="avatar">{{ name }}</div>'
                    },
                    'v-badge': {
                        props: ['content'],
                        template: '<span class="badge">{{ content }}</span>'
                    }
                }
            }
        })

    it('renders the conversation name', () => {
        expect(createWrapper().text()).toContain('John Doe')
    })

    it('renders the last message and time', () => {
        const wrapper = createWrapper()

        expect(wrapper.text()).toContain('Hello there')
        expect(wrapper.text()).toContain('10:30')
    })

    it('shows the online indicator when the conversation is online', () => {
        const wrapper = createWrapper()

        expect(wrapper.find('.online-indicator').exists()).toBe(true)
    })

    it('shows the unread badge', () => {
        const wrapper = createWrapper()

        expect(wrapper.find('.badge').text()).toBe('3')
    })

    it('emits select with the conversation id when clicked', async () => {
        const wrapper = createWrapper()

        await wrapper.trigger('click')

        expect(wrapper.emitted('select')).toEqual([['1']])
    })
})