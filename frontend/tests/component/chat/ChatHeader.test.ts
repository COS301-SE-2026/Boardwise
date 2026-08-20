import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatHeader from '~/components/features/chat/ChatHeader.vue'

const conversation = {
    id: '1',
    name: 'John Doe',
    avatar: '/john.jpg',
    online: true
}

describe('ChatHeader', () => {
    const createWrapper = (online = true) =>
        mount(ChatHeader, {
            props: {
                conversation: {
                    ...conversation,
                    online
                }
            },
            global: {
                stubs: {
                    BaseCard: { template: '<div><slot /></div>' },
                    BaseAvatar: {
                        props: ['name'],
                        template: '<div>{{ name }}</div>'
                    },
                    BaseButton: {
                        template: '<button><slot /></button>'
                    }
                }
            }
        })

    it('renders the conversation name', () => {
        expect(createWrapper().text()).toContain('John Doe')
    })

    it('shows Online when the conversation is online', () => {
        expect(createWrapper(true).text()).toContain('Online')
    })

    it('shows Offline when the conversation is offline', () => {
        expect(createWrapper(false).text()).toContain('Offline')
    })

    it('renders the About button', () => {
        expect(createWrapper().text()).toContain('About')
    })
})