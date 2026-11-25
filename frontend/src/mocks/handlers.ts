import { createOpenApiHttp } from 'openapi-msw'
import type { paths } from '../api/schema'

const http = createOpenApiHttp<paths>()

// GET /api/v1/templates 가짜 응답. 본문 모양은 계약 타입으로 검사된다.
// 계약에 없는 동작은 지어내지 않는다.
export const handlers = [
  http.get('/api/v1/templates', ({ response }) =>
    response(200).json({
      content: [
        {
          id: 1,
          title: '취업 준비 플래너',
          format: 'NOTION',
          category: 'CAREER',
          accessType: 'GENERAL',
          downloadCount: 37,
          thumbnailUrl: null,
          creator: { name: 'damda', official: true },
        },
        {
          id: 2,
          title: '발표용 깔끔한 PPT 세트',
          format: 'PPT',
          category: 'PRESENTATION',
          accessType: 'GENERAL',
          downloadCount: 12,
          thumbnailUrl: null,
          creator: { name: 'damda', official: true },
        },
      ],
      page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
    }),
  ),
]
