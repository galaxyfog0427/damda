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
    // 실패는 ProblemDetail이다. 화면 문장은 detail을 그대로 쓴다.
    // 500은 일반 문장을 보여 준다.
    if (result.response.status >= 500 && result.response.status !== 501) {
      throw new ApiProblem('서버에 문제가 생겼습니다. 잠시 뒤 다시 시도해 주세요.')
    }
    // problem+json이 아닌 응답(예: Spring이 꺼져 프록시가 돌려준 오류)에는 detail이 없다.
    const { detail } = result.error
    throw new ApiProblem(typeof detail === 'string' ? detail : '서버에 연결하지 못했습니다.')
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
