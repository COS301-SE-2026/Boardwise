import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityBanner from '~/components/features/community/CommunityBanner.vue'

describe('CommunityBanner', () => {
  const community = {
    id: '1',
    name: 'Board Game Club',
    description: 'A community for board game lovers',
    imageUrl: '/images/community.jpg',
    visibility: 'Public',
    memberCount: 25,
    isOwner: true
  }

  const mountComponent = () => {
    return mount(CommunityBanner, {
      props: {
        community
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseImage: {
            template: '<img :src="src" :alt="alt" />',
            props: ['src', 'alt']
          },
          BaseBadge: {
            template: '<span><slot /></span>'
          },
          BaseButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          CommunityEditModal: {
            template: '<div />'
          }
        }
      }
    })
  }

  it('renders community name', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Board Game Club')
  })

  it('renders community description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'A community for board game lovers'
    )
  })

  it('renders visibility', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Public')
  })

  it('renders member count', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Members (25)')
  })

  it('renders Events button', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Events')
  })

  it('renders Edit community for owner', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Edit community')
  })

  it('emits members when Members is clicked', async () => {
    const wrapper = mountComponent()

    const buttons = wrapper.findAll('button')

    await buttons[0].trigger('click')

    expect(wrapper.emitted('members')).toBeTruthy()
  })

  it('emits events when Events is clicked', async () => {
    const wrapper = mountComponent()

    const buttons = wrapper.findAll('button')

    await buttons[1].trigger('click')

    expect(wrapper.emitted('events')).toBeTruthy()
  })
})