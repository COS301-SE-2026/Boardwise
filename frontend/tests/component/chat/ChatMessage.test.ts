import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatMessage from '~/components/features/chat/ChatMessage.vue'

const message = {
    id: 1,
    name: 'John',
    avatar: '/john.jpg',
    text: 'Hello world',
    time: '10:30',
    isOwn: false
}

describe('ChatMessage', () => {
    it('renders the message text', () => {
        const wrapper = mount(ChatMessage, {
            props: { message },
            global: {
                stubs: {
                    BaseAvatar: true
                }
            }
        })

        expect(wrapper.text()).toContain('Hello world')
    })

    it('renders the message time', () => {
        const wrapper = mount(ChatMessage, {
            props: { message },
            global: {
                stubs: {
                    BaseAvatar: true
                }
            }
        })

        expect(wrapper.text()).toContain('10:30')
    })

    it('renders the avatar for another user', () => {
        const wrapper = mount(ChatMessage, {
            props: { message },
            global: {
                stubs: {
                    BaseAvatar: {
                        template: '<div class="avatar" />'
                    }
                }
            }
        })

        expect(wrapper.find('.avatar').exists()).toBe(true)
    })

    it('applies the own message class for the current user', () => {
        const wrapper = mount(ChatMessage, {
            props: {
                message: {
                    ...message,
                    isOwn: true
                }
            },
            global: {
                stubs: {
                    BaseAvatar: true
                }
            }
        })

        expect(wrapper.find('.bubble').classes()).toContain('own')
    })
})