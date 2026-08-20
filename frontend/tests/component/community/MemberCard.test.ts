import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MemberCard from '~/components/features/community/MemberCard.vue'

describe('MemberCard', () => {
  const member = {
    id: '1',
    username: 'Thabo',
    profilePicture: '/images/thabo.jpg'
  }

  it('renders the member username', () => {
    const wrapper = mount(MemberCard, {
      props: {
        member
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseAvatar: {
            props: ['src', 'name'],
            template: '<div class="avatar">{{ name }}</div>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('Thabo')
  })

  it('renders the member avatar', () => {
    const wrapper = mount(MemberCard, {
      props: {
        member
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div><slot /></div>'
          },
          BaseAvatar: {
            props: ['src'],
            template: '<div class="avatar"></div>'
          }
        }
      }
    })

    expect(wrapper.find('.avatar').exists()).toBe(true)
  })
})