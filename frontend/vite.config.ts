import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  build: {
    assetsDir: 'app-assets',
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/assets': 'http://localhost:8080',
      '/ws': 'http://localhost:8080',
      '/css': 'http://localhost:8080',
      '/js': 'http://localhost:8080',
      '/images': 'http://localhost:8080',
      '/favicon.svg': 'http://localhost:8080',
      '/icons.svg': 'http://localhost:8080',
    },
  },
})
