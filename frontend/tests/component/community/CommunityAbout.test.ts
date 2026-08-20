import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityAbout from '~/components/features/community/CommunityAbout.vue'

describe('CommunityAbout', () => {
  const community = {
    id: '1',
    name: 'Board Game Club',
    description: 'A community for board game lovers.',
    members: 25
  }

  const mountComponent = () => {
    return mount(CommunityAbout, {
      props: {
        community
      },
      global: {
        stubs: {
          BaseCard: {
            template: '<div class="card"><slot /></div>'
          },
          BaseInput: {
            template: '<div><slot /></div>'
          }
        }
      }
    })
  }

  it('renders the community description', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain(
      'A community for board game lovers.'
    )
  })

  it('renders the member count', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('25 members')
  })

  it('renders community rules', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Rules')
    expect(wrapper.text()).toContain('Be respectful')
  })
})