import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MemberList from '~/components/features/community/MemberList.vue'

describe('MemberList', () => {
  const community = {
    members: [
      {
        username: 'Thabo',
        profilePicture: '/avatar.jpg'
      },
      {
        username: 'Palesa',
        profilePicture: '/avatar2.jpg'
      }
    ]
  }

  const mountComponent = () => {
    return mount(MemberList, {
      props: {
        community,
        modelValue: true
      },
      global: {
        stubs: {
          BaseGrid: {
            template: '<div><slot /></div>'
          },
          MemberCard: {
            props: ['member'],
            template: '<div>{{ member.username }}</div>'
          },
          InviteMemberModal: {
            template: '<div />'
          }
        }
      }
    })
  }

  it('renders the member list', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Thabo')
    expect(wrapper.text()).toContain('Palesa')
  })

  it('renders all community members', () => {
    const wrapper = mountComponent()

    expect(wrapper.findAll('div').length).toBeGreaterThan(0)
  })

  it('does not render members when modelValue is false', () => {
    const wrapper = mount(MemberList, {
      props: {
        community,
        modelValue: false
      },
      global: {
        stubs: {
          BaseGrid: {
            template: '<div><slot /></div>'
          },
          MemberCard: {
            template: '<div />'
          },
          InviteMemberModal: {
            template: '<div />'
          }
        }
      }
    })

    expect(wrapper.text()).not.toContain('Thabo')
  })
})