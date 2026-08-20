import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatConversationList from '~/components/features/chat/ChatConversationList.vue'

const conversations = [
    {
        id: '1',
        name: 'John',
        avatar: '/john.jpg',
        time: '10:00',
        lastMessage: 'Hello',
        online: true,
        unread: 2
    },
    {
        id: '2',
        name: 'Jane',
        avatar: '/jane.jpg',
        time: '11:00',
        lastMessage: 'Hi',
        online: false,
        unread: 0
    }
]

describe('ChatConversationList', () => {
    it('renders all conversations', () => {
        const wrapper = mount(ChatConversationList, {
            props: {
                conversations
            },
            global: {
                stubs: {
                    ChatConversationCard: {
                        props: ['conversation', 'active'],
                        template: `
                            <div class="conversation-card">
                                {{ conversation.name }}
                            </div>
                        `
                    }
                }
            }
        })

        expect(wrapper.findAll('.conversation-card')).toHaveLength(2)
        expect(wrapper.text()).toContain('John')
        expect(wrapper.text()).toContain('Jane')
    })

    it('passes the selected state to the correct conversation', () => {
        const wrapper = mount(ChatConversationList, {
            props: {
                conversations,
                selected: '2'
            },
            global: {
                stubs: {
                    ChatConversationCard: {
                        props: ['conversation', 'active'],
                        template: `
                            <div
                                class="conversation-card"
                                :data-active="active"
                            >
                                {{ conversation.name }}
                            </div>
                        `
                    }
                }
            }
        })

        const cards = wrapper.findAll('.conversation-card')

        expect(cards[0].attributes('data-active')).toBe('false')
        expect(cards[1].attributes('data-active')).toBe('true')
    })

    it('forwards the select event', async () => {
        const wrapper = mount(ChatConversationList, {
            props: {
                conversations
            },
            global: {
                stubs: {
                    ChatConversationCard: {
                        template: '<button @click="$emit(\'select\', \'1\')">Select</button>'
                    }
                }
            }
        })

        await wrapper.find('button').trigger('click')

        expect(wrapper.emitted('select')).toEqual([['1']])
    })
})