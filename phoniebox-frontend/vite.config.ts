import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },

  server: {
    port: 5173,
    proxy: {
      // Forward all /api requests to the Quarkus backend during development.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  build: {
    // Output goes into the Quarkus static-resource directory so it is
    // bundled into the application JAR / native binary automatically.
    outDir: '../phoniebox-app/src/main/resources/META-INF/resources',
    emptyOutDir: true,
  },
})
