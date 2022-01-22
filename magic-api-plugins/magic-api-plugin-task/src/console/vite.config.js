import vue from '@vitejs/plugin-vue'
import viteSvgIcons from 'vite-plugin-svg-icons'
import path from 'path'
import pkg from './package.json'

export default {
    base: './',
    build: {
        minify: false,
        cssCodeSplit: true, // 将组件的 style 打包到 js 文件中
        outDir: 'dist',
        lib: {
            target: 'esnext',
            formats: ['iife'],
            entry: path.resolve(__dirname, 'src/index.js'),
            name: 'MagicTask',
            fileName: (format) => `magic-task.${pkg.version}.${format}.js`
        },
        rollupOptions: {
            // 确保外部化处理那些你不想打包进库的依赖
            external: ['vue'],
            output: {
                // 在 UMD 构建模式下为这些外部化的依赖提供一个全局变量
                globals: {
                    vue: 'Vue'
                }
            }
        }
    },
    plugins: [
        vue(),
        viteSvgIcons({
			iconDirs: [path.resolve(process.cwd(), 'src/icons')],
			symbolId: 'magic-task-[name]'
		}),
    ]
}