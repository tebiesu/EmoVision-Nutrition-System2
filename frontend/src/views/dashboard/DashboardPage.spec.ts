// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardPage from '@/views/dashboard/DashboardPage.vue'

vi.mock('@/api/dashboard', () => ({
  getDashboardSummaryApi: vi.fn(async () => ({
    todayMealCount: 2,
    latestMeal: {
      assessment: { riskLevel: 'LOW' },
      recommendation: {
        summaryText: '这餐整体比较均衡',
        recommendationText: '建议保持规律饮水，下一餐继续补充优质蛋白。'
      }
    },
    dailySummary: {
      totalCalories: 680,
      avgEmotionScore: 4.2,
      summaryText: '今日摄入较稳定，情绪整体平稳。'
    }
  }))
}))

describe('DashboardPage', () => {
  it('renders dashboard summary', async () => {
    const wrapper = mount(DashboardPage)
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('今日饮食与情绪总览')
    expect(wrapper.text()).toContain('这餐整体比较均衡')
    expect(wrapper.text()).toContain('今日摄入较稳定，情绪整体平稳。')
  })
})
