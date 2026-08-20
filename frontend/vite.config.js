import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // 단위 테스트는 src 아래만 본다. e2e는 Playwright 가 맡는다
  test: {
    include: ['src/**/*.test.{js,jsx}'],
    environment: 'jsdom',
    restoreMocks: true,
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // SockJS는 /ws/info 폴링과 웹소켓 업그레이드를 모두 사용한다
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
      }
    }
  }
})
