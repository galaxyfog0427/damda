import { useQuery } from '@tanstack/react-query'
import { api } from './api/client'

// 서버 문장(detail)을 그대로 보여 주기 위해 Error로 감싸 던진다.
class ApiProblem extends Error {}

async function fetchTemplates() {
  let result
  try {
    result = await api.GET('/api/v1/templates')
  } catch {
    throw new ApiProblem('서버에 연결하지 못했습니다.')
  }
  if (result.error !== undefined) {
    // 서버가 준 실패는 ProblemDetail(application/problem+json)이다.
    // 그 밖의 응답(예: Spring이 꺼져 Vite 프록시가 돌려준 502 text/plain)에는 detail이 없으므로
    // 상태 코드와 상관없이 "서버에 연결하지 못했습니다."를 보여 준다.
    const isProblem = result.response.headers.get('content-type')?.includes('application/problem+json')
    const { detail } = result.error
    if (!isProblem || typeof detail !== 'string') {
      throw new ApiProblem('서버에 연결하지 못했습니다.')
    }
    // problem+json인 5xx(501 제외)는 일반 문장을 보여 주고, 나머지는 detail을 그대로 쓴다.
    if (result.response.status >= 500 && result.response.status !== 501) {
      throw new ApiProblem('서버에 문제가 생겼습니다. 잠시 뒤 다시 시도해 주세요.')
    }
    throw new ApiProblem(detail)
  }
  if (result.data === undefined) {
    throw new ApiProblem('서버에 연결하지 못했습니다.')
  }
  return result.data
}

export default function App() {
  const { data, error, isPending } = useQuery({
    queryKey: ['templates'],
    queryFn: fetchTemplates,
    retry: false,
  })

  return (
    <main>
      <h1>템플릿 목록(확인용)</h1>
      {isPending && <p>불러오는 중</p>}
      {error && <p role="alert">{error.message}</p>}
      {data && (
        <ul>
          {data.content.map((template) => (
            <li key={template.id}>{template.title}</li>
          ))}
        </ul>
      )}
    </main>
  )
}
