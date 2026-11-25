/// <reference types="vite/client" />
/// <reference types="msw/vite/client" />

interface ImportMetaEnv {
  /** 'off'면 개발 화면에서도 가짜 응답을 켜지 않고 진짜 Spring에 붙는다. */
  readonly VITE_MOCK?: string
}
