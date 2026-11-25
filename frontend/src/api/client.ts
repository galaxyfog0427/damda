import createClient from 'openapi-fetch'
import type { components, paths } from './schema'

// API 타입은 생성된 schema.d.ts에서만 가져온다 (ADR-0006).
// 주소는 /api/v1/... 이고, 개발에서는 Vite 프록시가 Spring으로 넘긴다.
export const api = createClient<paths>()

export type ErrorResponse = components['schemas']['ErrorResponse']
