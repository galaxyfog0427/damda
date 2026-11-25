import react from '@vitejs/plugin-react'
import { msw } from 'msw/vite'
import { defineConfig } from 'vite'

// 개발: 가짜 응답이 없는 /api 요청은 Spring으로 넘어간다 (ADR-0005, ADR-0006).
// msw 플러그인은 개발 서버에서 워커 파일을 내주고, 빌드에는 src가 import하지 않으면 넣지 않는다.
export default defineConfig({
  plugins: [react(), msw()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
