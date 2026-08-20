import { mount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'

const {
    getMessages,
    fetchInvites,
    respondToInvite,
    show
} = vi.hoisted(() => ({
    getMessages: vi.fn(() => [
        {
            id: 1,
            name: 'John',
            avatar: '/john.jpg',
            text: 'Hello',
            time: '10:00',
            isOwn: false
        }
    ]),
    fetchInvites: vi.fn(),
    respondToInvite: vi.fn(),
    show: vi.fn()
}))

vi.mock('~/services/chatService.js', () => ({
    getMessages
}))

vi.mock('~/composables/useEvents', () => ({
    useEvents: () => ({
        invites: [],
        fetchInvites,
        respondToInvite
    })
}))

vi.mock('#imports', () => ({
    useSnackBar: () => ({
        show
    })
}))

import ChatWindow from '~/components/features/chat/ChatWindow.vue'

describe('ChatWindow', () => {
    const conversation = {
        id: '1',
        name: 'John',
        avatar: '/john.jpg',
        online: true,
        isInvite: false
    }

    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('loads messages for the selected conversation', async () => {
        mount(ChatWindow, {
            props: {
                conversation
            },
            global: {
                stubs: {
                    ChatHeader: true,
                    ChatFeed: {
                        props: ['messages'],
                        template: '<div class="chat-feed">{{ messages.length }}</div>'
                    },
                    ChatComposer: true,
                    InviteFeed: true
                }
            }
        })

        await Promise.resolve()

        expect(getMessages).toHaveBeenCalledWith('1')
    })

    it('renders the chat components for a normal conversation', () => {
        const wrapper = mount(ChatWindow, {
            props: {
                conversation
            },
            global: {
                stubs: {
                    ChatHeader: true,
                    ChatFeed: true,
                    ChatComposer: true,
                    InviteFeed: true
                }
            }
        })

        expect(wrapper.findComponent({ name: 'ChatHeader' }).exists()).toBe(true)
        expect(wrapper.findComponent({ name: 'ChatFeed' }).exists()).toBe(true)
        expect(wrapper.findComponent({ name: 'ChatComposer' }).exists()).toBe(true)
    })

    it('renders InviteFeed for an invite conversation', () => {
        const wrapper = mount(ChatWindow, {
            props: {
                conversation: {
                    ...conversation,
                    id: 'invites',
                    isInvite: true
                }
            },
            global: {
                stubs: {
                    ChatHeader: true,
                    ChatFeed: true,
                    ChatComposer: true,
                    InviteFeed: {
                        template: '<div class="invite-feed">Invites</div>'
                    }
                }
            }
        })

        expect(wrapper.find('.invite-feed').exists()).toBe(true)
    })
})