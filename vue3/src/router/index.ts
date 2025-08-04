import {createRouter, createWebHistory} from 'vue-router'
import LoginView from "@/views/LoginView.vue";
import RegisterView from "@/views/RegisterView.vue";
import mainView from "@/views/mainView.vue";
import HomeView from '@/views/HomeView.vue'
import AdminView from "@/views/AdminView.vue";
import BookView from "@/views/HistoryView.vue";

const router = createRouter({
    history: createWebHistory(), // 路由器工作模式
    routes: [
        {
            path: '/login',
            name: 'login',
            component: LoginView
        },
        {
            path: '/register',
            name: 'register',
            component: RegisterView
        },
        {
            path: '/',
            name: 'main',
            component: mainView,
            children: [
                {
                    path: '',
                    name: 'home',
                    component: HomeView
                },
                {
                    path: 'admin',
                    name: 'admin',
                    component: AdminView
                },
                {
                    path: 'book',
                    name: 'book',
                    component: BookView
                }
            ]
        },

    ]
})

//路由守卫
router.beforeEach((to, _from, next) => {
    const publicPages = ['/login', '/register'];
    const loggedIn = localStorage.getItem('user');
    const storedUser = localStorage.getItem("user");
    const user = storedUser ? JSON.parse(storedUser) : {}

    //未登录，访问需要登录的页面，跳转到登录页面
    if (!publicPages.includes(to.path) && !loggedIn) {
        return next('/login');
    }
    //学生，访问/admin页面，跳转到首页
    if ('/admin' === to.path && loggedIn && user.role === 'role_student') {
        return next('/');
    }

    next();
})

export default router
