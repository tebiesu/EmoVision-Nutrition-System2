// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardPage from '@/views/dashboard/DashboardPage.vue'

vi.mock('@/api/dashboard', () => ({
  getDashboardSummaryApi: vi.fn(async () => ({
    todayMealCount: 2,
    latestMeal: {
      assessment: { riskLevel: 'LOW' },
      recommendation: { summaryText: 'Balanced meal', recommendationText: 'Keep hydration stable.' }
    },
    dailySummary: { totalCalories: 680, avgEmotionScore: 4.2, summaryText: 'Stable intake today.' }
  }))
}))

describe('DashboardPage', () => {
  it('renders dashboard summary', async () => {
    const wrapper = mount(DashboardPage)
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('今日饮食与情绪总览')
    expect(wrapper.text()).toContain('Balanced meal')
    expect(wrapper.text()).toContain('Stable intake today.')
  })
})
