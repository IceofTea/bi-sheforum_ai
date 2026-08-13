import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue';
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    //首页
    {
      path:'/',
      name: '',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          alias:'index',
          name: 'index',
          component: () => import('@/views/index/index.vue')
        },
      ]
    },
    // 登录
    {
      path: '/user',
      meta: { requiresAuth: false },
      children: [
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/user/login.vue')
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('@/views/user/register.vue'),
        },
      ]
    },
    {
      path: '/user',
      name: 'user',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'edit',
          name: 'useredit',
          component: () => import('@/views/user/edit.vue'),
        },
        {
          path: 'personal',
          name: 'personal',
          component: () => import('@/views/user/personal.vue'),
        },
      ]
    },
    {
      path: '/thread',
      name: 'thread',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'threadlist',
          component: () => import('@/views/thread/list.vue')
        },
        {
          path: 'amount',
          name: 'threadamount',
          component: () => import('@/views/thread/amount.vue')
        },
        {
          path: 'add',
          name: 'threadadd',
          component: () => import('@/views/thread/add.vue')
        },
        {
          path: 'edit',
          name: 'threadedit',
          component: () => import('@/views/thread/edit.vue')
        },
      ]
    },
    {
      path: '/sort',
      name: 'threadsort',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'sortlist',
          component: () => import('@/views/threadsort/list.vue')
        },
        {
          path: 'add',
          name: 'sortadd',
          component: () => import('@/views/threadsort/add.vue')
        },
        {
          path: 'edit',
          name: 'sortedit',
          component: () => import('@/views/threadsort/edit.vue')
        }
      ]
    },
    {
      path: '/reader',
      name: 'reader',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'readerlist',
          component: () => import('@/views/reader/list.vue')
        }, 
        {
          path: 'detail',
          name: 'readerdetail',
          component: () => import('@/views/reader/detail.vue')
        },
        {
          path: 'action',
          name: 'readeraction',
          component: () => import('@/views/reader/actionlist.vue')
        },
        {
          path: 'statistics',
          name: 'readerstatistics',
          component: () => import('@/views/reader/infostatistics.vue')
        },
      ]
    },
    {
      path: '/writer',
      name: 'writer',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'list',
          name: 'writerlist',
          component: () => import('@/views/writer/list.vue')
        },
        {
          path: 'add',
          name: 'writeradd',
          component: () => import('@/views/writer/add.vue')
        },
        {
          path: 'edit',
          name: 'writeredit',
          component: () => import('@/views/writer/edit.vue')
        },
      ]
    },
    {
      path: '/comment',
      name: 'comment',
      component: Layout,
      meta: { requiresAuth: false },
      children: [
        {
          path: 'list',
          name: 'commentlist',
          component: () => import('@/views/comment/list.vue')
        },
      ]
    },
  ]
})
//全局前置守卫
router.beforeEach((to, from) => {
  //判断是否需要权限登录验证
  if (!to.meta.requiresAuth) {
    return true;
  }
  //提前token校验
  let token = sessionStorage.token;
  if (!token) {
    //重定向至登录页,附带redirect参数
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})
export default router
