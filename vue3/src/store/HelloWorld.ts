import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
export default pinia;

import {defineStore} from "pinia";

export const useHelloWorldStore = defineStore('HelloWorld', {
    state() {
        return {
            count: 6
        }
    },
    getters: {},
    actions: {
        increment(value: number) {
            this.count += value;
        }
    },
    persist: true
})