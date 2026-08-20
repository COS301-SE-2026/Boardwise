import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CommunityGrid from '~/components/features/community/CommunityGrid.vue'

describe('CommunityGrid', () => {
  const communities = [
    {
      id: '1',
      name: 'Board Game Club',
      description: 'Board games',
      imageUrl: '/community1.jpg',
      visibility: 'Public'
    },
    {
      id: '2',
      name: 'Strategy Games',
      description: 'Strategy games',
      imageUrl: '/community2.jpg',
      visibility: 'Private'
    }
  ]

  const mountComponent = (items = communities) => {
    return mount(CommunityGrid, {
      props: {
        communities: items
      },
      global: {
        stubs: {
          BaseGrid: {
            template: '<div class="community-grid"><slot /></div>'
          },
          BaseEmptyState: {
            props: ['title'],
            template: '<div class="empty-state">{{ title }}</div>'
          },
          CommunityCard: {
            props: ['community'],
            template: `
              <div class="community-card">
                {{ community.name }}
              </div>
            `
          }
        }
      }
    })
  }

  it('renders all communities', () => {
    const wrapper = mountComponent()

    const cards = wrapper.findAll('.community-card')

    expect(cards).toHaveLength(2)
    expect(wrapper.text()).toContain('Board Game Club')
    expect(wrapper.text()).toContain('Strategy Games')
  })

  it('shows empty state when there are no communities', () => {
    const wrapper = mountComponent([])

    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.text()).toContain('No communities yet')
  })

  it('renders the community grid when communities exist', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('.community-grid').exists()).toBe(true)
  })
})