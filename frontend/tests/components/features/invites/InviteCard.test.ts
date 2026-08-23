import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import InviteCard from '~/components/features/invites/InviteCard.vue'
import type { InviteItem } from '~/services/eventService'

describe('InviteCard', () => {
    const mockInvite = {
        event: { 
            id: '1', 
            name: 'Epic Catan Night', 
            date: '2026-08-25', 
            image:'/images/catan.png'
        }, 
        host: {
            username: 'iAmNotR3alBro'
        },
        status: 'pending'
    } as InviteItem

    const mountComponent = (inviteProp = mockInvite) => {
        return mount(InviteCard, {
            props: {
                invite: inviteProp
            }, 
            global: {
                stubs: {
                    BaseCard: {
                        template: '<div data-test="base-card"><slot /></div>'
                    },
                    BaseImage: {
                        props: ['src', 'alt'],
                        template: '<img data-test="base-image" :src="src" :alt="alt" />'
                    },
                    VBtn: {
                        template: '<div data-test="v-btn" @click="$emit(\'click\')"><slot /></div>'
                    }
                }
            }
        })
    }

    it('renders the event details correctly', () => {
        const wrapper = mountComponent()

        expect(wrapper.text()).toContain('Epic Catan Night')
        expect(wrapper.text()).toContain('iAmNotR3alBro')
        expect(wrapper.text()).toContain('2026-08-25')
    })

    it('passes the correct src and alt text to BaseImage', () => {
        const wrapper = mountComponent()

        const imageComponent = wrapper.findComponent('[data-test="base-image"]')

        expect(imageComponent.attributes('src')).toBe('/images/catan.png')
        expect(imageComponent.attributes('alt')).toBe('Epic Catan Night')
    })

    it('emits "accept" with the event ID when the Accept button is clicked', async () => {
        const wrapper = mountComponent()

        const acceptButton = wrapper.find('[data-test="accept-button"]')
        await acceptButton.trigger('click')

        expect(wrapper.emitted('accept')).toBeTruthy()
        expect(wrapper.emitted('accept')![0]).toEqual([mockInvite.event.id])
    })

    it('emits "decline" with the event ID when the Decline button is clicked', async () => {
        const wrapper = mountComponent()

        const declineButton = wrapper.find('[data-test="decline-button"]')
        await declineButton.trigger('click')

        expect(wrapper.emitted('decline')).toBeTruthy()
        expect(wrapper.emitted('decline')![0]).toEqual([mockInvite.event.id])
    })
})