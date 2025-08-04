import axios from "axios";
/**
对request进行封装，可以方便的直接调用，不用每次都引入axios
 */

//创建一个axios对象出来
const request = axios.create({
    baseURL: 'http://localhost:8080/api',//请求的基础路径。webconfig增加了路径前缀，这里也要对应
    timeout: 10000
})

//request拦截器，可以自请求发送前对请求做一些处理，例如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
        //请求前做一些处理
        config.headers['Content-Type'] = 'application/json;charset=UTF-8';
        //前台获取token，下次的每个http请求，header都加上token
        const user = localStorage.getItem('user')
        if (user){
            config.headers['token'] = JSON.parse(user).token;//将token放入请求头
        }
        return config
    },
    error => {
        //请求错误处理
        return Promise.reject(error)
    }
)

//response拦截器，可以对响应数据做一些处理
request.interceptors.response.use(
    response => {
        //响应数据处理
        let res = response.data;//获取到的数据，result对象
        //兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }
        return res
    },
    error => {
        //响应错误处理
        console.log('err' + error) // for debug
        return Promise.reject(error)
    }
)

export default request