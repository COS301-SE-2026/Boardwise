import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import InviteFeed from '~/components/features/invites/InviteFeed.vue'

describe('InviteFeed', () => {
    const mockInvites = [
        {
            event: { id: 1, title: 'Game Night', date: '2026-08-25'},
            status: 'pending'
        },
        {
            event: { id: 2, title: 'Chess Night', date: '2026-09-25'},
            status: 'pending'
        }
    ]

    const mountComponent = (props = {}) => {
        return mount(InviteFeed, {
            props, 
            global: {
                stubs: {
                    BaseCard: {
                        template: '<div data-test"base-card"><slot /></div>'
                    },
                    BaseEmptyState: {
                        template: '<div data-test"base-empty-state">{{ title }} - {{ description }}</div>'
                    },
                    InviteCard: {
                        template: '<div data-test="invite-card"></div>'
                    }
                }
            }
        })
    }

    it('renders the BaseCard wrapper', () => {
        const wrapper = mountComponent()
        expect(wrapper.find('[data-test="base-card"').exists()).toBe(true)
    })

    it('renders the BaseEmptyState when the invites array is empty', () => {
        const wrapper = mountComponent({ invites: [] })

        expect(wrapper.find('[data-test="base-empty-state"').exists()).toBe(true)
        expect(wrapper.find('[data-test="invite-card"').exists()).toBe(false)
    })

    it('renders a list of InviteCards when invites are provided', () => {
        const wrapper = mountComponent({ invites: mockInvites})

        const inviteCards = wrapper.findAll('[data-test="invite-card"]')

        expect(wrapper.find('[data-test="base-empty-state"]').exists).toBe(false)
        expect(inviteCards).toHaveLength(mockInvites.length)
    })

    it('emits "accept" when an InviteCard emits "accept"', async () => {
        const wrapper = mountComponent({ invites: mockInvites })
        const firstInviteCard = wrapper.findComponent('[data-test="invite-card"]')

        await (firstInviteCard as any).$emit('accept', mockInvites[0]!.event.id)

        expect(wrapper.emitted('accept')).toBeTruthy()
        expect(wrapper.emitted('accept')![0]).toEqual([mockInvites[0]?.event.id])
    })

    it('emits "decline" when an InviteCard emits "decline"', async () => {
        const wrapper = mountComponent({ invites: mockInvites })
        const firstInviteCard = wrapper.findComponent('[data-test="invite-card"]')

        await (firstInviteCard as any).$emit('decline', mockInvites[0]!.event.id)

        expect(wrapper.emitted('decline')).toBeTruthy()
        expect(wrapper.emitted('decline')![0]).toEqual([mockInvites[0]?.event.id])
    })
})