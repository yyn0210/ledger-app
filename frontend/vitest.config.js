import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx,vue}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      threshold: {
        lines: 70,
        functions: 70,
        branches: 70,
        statements: 70
      },
      include: ['src/**/*.{js,ts,vue}'],
      exclude: [
        'src/main.js',
        'src/router/index.js',
        'src/**/mock.js',
        'src/**/*.spec.{js,ts,vue}',
        'src/**/*.test.{js,ts,vue}'
      ]
    },
    setupFiles: ['./src/test/setup.js']
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
