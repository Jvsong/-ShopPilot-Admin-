import { http } from '@/utils/request'

export type AiQuestionType = 'HOT_PRODUCTS' | 'RESTOCK_SUGGESTION' | 'ORDER_ALERT' | 'DAILY_SUMMARY'
export type AiRangeType = '7d' | '15d' | '30d'

export interface AiAssistantSuggestion {
  title: string
  level: 'HIGH' | 'MEDIUM' | 'LOW'
  detail: string
  action: string
  actionTarget: string
  evidence: string[]
}

export interface AiAssistantResponse {
  questionType: AiQuestionType
  startDate: string
  endDate: string
  summary: string
  suggestions: AiAssistantSuggestion[]
  metrics: Record<string, number | string>
}

export interface AiAssistantQuery {
  questionType?: AiQuestionType
  rangeType?: AiRangeType
  startDate?: string
  endDate?: string
}

export const aiAssistantApi = {
  ask(params: AiAssistantQuery) {
    return http.get<AiAssistantResponse>('/admin/ai-assistant/ask', params)
  }
}

export default aiAssistantApi
