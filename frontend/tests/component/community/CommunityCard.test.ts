import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityCard from '~/components/features/community/CommunityCard.vue'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

describe('CommunityCard', () => {
  const community = {
    id: '1',
    name: 'Board Game Club',
    description: 'A community for board game lovers.',
    imageUrl: '/images/community.jpg',
    visibility: 'Public'
  }

  const mountComponent = () => {
    return mount(CommunityCard, {
      props: {
        community
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div class="base-card"><slot /></div>'
          },
          BaseImage: {
            props: ['src', 'alt', 'height'],
            template: '<img class="community-image" :src="src" :alt="alt" />'
          },
          BaseBadge: {
            props: ['variant'],
            template: '<span class="badge"><slot /></span>'
          },
          BaseButton: {
            template: '<button><slot /></button>'
          }
        }
      }
    })
  }

  it('renders the community name', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Board Game Club')
  })

  it('renders the community description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'A community for board game lovers.'
    )
  })

  it('renders the community visibility', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Public')
  })

  it('renders the community image', () => {
    const wrapper = mountComponent()

    const image = wrapper.find('.community-image')

    expect(image.exists()).toBe(true)
    expect(image.attributes('src')).toBe('/images/community.jpg')
  })

  it('navigates to the community when View is clicked', async () => {
    const wrapper = mountComponent()

    await wrapper.find('button').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/community/1')
  })
})