/// <reference types="vite/client" />
//告诉 TypeScript：凡是以 .vue 结尾的模块，都是 Vue 组件,才能找到组件
declare module '*.vue' {
    import { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}
