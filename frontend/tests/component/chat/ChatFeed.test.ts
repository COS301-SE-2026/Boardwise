import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatFeed from '~/components/features/chat/ChatFeed.vue'

describe('ChatFeed', () => {
    const message = {
        id: 1,
        name: 'John',
        avatar: '/john.jpg',
        text: 'Hello',
        time: '10:00',
        isOwn: false
    }

    it('renders messages when messages are provided', () => {
        const wrapper = mount(ChatFeed, {
            props: {
                messages: [message]
            },
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    ChatMessage: {
                        props: ['message'],
                        template: '<div class="chat-message">{{ message.text }}</div>'
                    },
                    BaseEmptyState: true
                }
            }
        })

        expect(wrapper.find('.chat-message').exists()).toBe(true)
        expect(wrapper.text()).toContain('Hello')
    })

    it('shows the empty state when there are no messages', () => {
        const wrapper = mount(ChatFeed, {
            props: {
                messages: []
            },
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    ChatMessage: true,
                    BaseEmptyState: {
                        props: ['title', 'description'],
                        template: '<div class="empty-state">{{ title }} {{ description }}</div>'
                    }
                }
            }
        })

        expect(wrapper.find('.empty-state').exists()).toBe(true)
        expect(wrapper.text()).toContain('No messages')
    })
})