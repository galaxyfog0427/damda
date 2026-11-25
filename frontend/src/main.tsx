import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'

// 개발 화면에서만 가짜 응답(MSW)을 켠다. VITE_MOCK=off 이면 켜지 않는다.
// import.meta.env.DEV로 감싸서 운영 빌드에는 MSW 코드가 들어가지 않는다.
async function enableMocking() {
  if (import.meta.env.DEV && import.meta.env.VITE_MOCK !== 'off') {
    const { network } = await import('virtual:msw')
    const { handlers } = await import('./mocks/handlers')
    network.configure({ handlers, onUnhandledFrame: 'bypass' })
    await network.enable()
  }
}

const queryClient = new QueryClient()

enableMocking().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    </StrictMode>,
  )
})
