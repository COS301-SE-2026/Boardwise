import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChatSidebar from '~/components/features/chat/ChatSidebar.vue'

const conversations = [
    {
        id: '1',
        name: 'John Doe',
        avatar: '/john.jpg',
        time: '10:00',
        lastMessage: 'Hello',
        online: true,
        unread: 2
    },
    {
        id: '2',
        name: 'Jane Doe',
        avatar: '/jane.jpg',
        time: '11:00',
        lastMessage: 'Hi',
        online: false,
        unread: 0
    }
]

const createWrapper = () =>
    mount(ChatSidebar, {
        props: {
            conversations
        },
        global: {
            stubs: {
                BaseCard: { template: '<div><slot /></div>' },
                BaseEmptyState: {
                    template: '<div class="empty-state">{{ title }}</div>',
                    props: ['title']
                },
                BaseSearch: {
                    props: ['modelValue'],
                    emits: ['update:modelValue'],
                    template: `
                        <input
                            :value="modelValue"
                            @input="$emit('update:modelValue', $event.target.value)"
                        />
                    `
                },
                SectionTitle: {
                    props: ['title'],
                    template: '<h2>{{ title }}</h2>'
                },
                BaseFilterGroup: {
                    template: '<div><slot /></div>'
                },
                ChatConversationList: {
                    props: ['conversations', 'selected'],
                    emits: ['select'],
                    template: `
                        <div>
                            <button
                                v-for="conversation in conversations"
                                :key="conversation.id"
                                class="conversation"
                                @click="$emit('select', conversation.id)"
                            >
                                {{ conversation.name }}
                            </button>
                        </div>
                    `
                },
                'v-chip-group': {
                    props: ['modelValue'],
                    emits: ['update:modelValue'],
                    template: '<div><slot /></div>'
                },
                'v-chip': {
                    props: ['value'],
                    template: '<button @click="$emit(\'click\')"><slot /></button>'
                }
            }
        }
    })

describe('ChatSidebar', () => {
    it('renders the Chats title', () => {
        expect(createWrapper().text()).toContain('Chats')
    })

    it('filters conversations by search', async () => {
        const wrapper = createWrapper()

        const input = wrapper.find('input')

        await input.setValue('John')

        expect(wrapper.text()).toContain('John Doe')
        expect(wrapper.text()).not.toContain('Jane Doe')
    })

    it('emits select when a conversation is selected', async () => {
        const wrapper = createWrapper()

        await wrapper.findAll('.conversation')[0].trigger('click')

        expect(wrapper.emitted('select')).toEqual([['1']])
    })
})