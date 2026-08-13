import { createRouter, createWebHistory } from 'vue-router'
import Layout from "../components/Layout.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/register.vue')
    },
    {
      path: '/',
      name: 'home',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          alias: 'index',
          name: 'index',
          component: () => import('../views/index/index.vue')
        },
        {
          path: 'allthread',
          name: 'allthread',
          component: () => import('../views/index/allthread.vue')
        },
        {
          path: 'threadlist',
          name: 'threadlist',
          component: () => import('../views/index/threadlist.vue')
        },
        {
          path: 'finishthread',
          name: 'finishthread',
          component: () => import('../views/index/finishthread.vue')
        },
        {
          path: 'uploadthread',
          name: 'uploadthread',
          component: () => import('../views/index/uploadthread.vue')
        },
        {
          path: 'showfile',
          name: 'showfile',
          component: () => import('../views/index/showfile.vue')
        },
        {
          path: 'sortthread',
          name: 'sortthread',
          component: () => import('../views/index/sortthread.vue')
        }
      ]
    },
    {
      path: '/thread',
      name: 'threads',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'index',
          name: 'threadindex',
          component: () => import('../views/thread/index.vue')
        }
      ]
    },
    {
      path: '/thread/read',
      name: 'threadread',
      meta: { requiresAuth: true },
      component: () => import('../views/thread/read.vue')
    },
    {
      path: '/user/index',
      name: 'userindex',
      meta: { requiresAuth: true },
      component: () => import('../views/user/index.vue'),
    },
    {
      path: '/user/read',
      name: 'userread',
      meta: { requiresAuth: true },
      component: () => import('../views/user/readhistory.vue'),
    },
    {
      path: '/user/myread',
      name: 'usermyread',
      meta: { requiresAuth: true },
      component: () => import('../views/user/myread.vue'),
    },
    {
      path: '/user/mycollect',
      name: 'usermycollect',
      meta: { requiresAuth: true },
      component: () => import('../views/user/mycollect.vue'),
    },
    {
      path: '/user/myscomments',
      name: 'usermyscomments',
      meta: { requiresAuth: true },
      component: () => import('../views/user/myscomments.vue'),
    },
    {
      path: '/user/mythreads',
      name: 'usermythreads',
      meta: { requiresAuth: true },
      component: () => import('../views/user/mythreads.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFound.vue')
    }
  ]
})

router.beforeEach((to, from) => {
  if (to.name === 'not-found') return true
  if (!to.meta.requiresAuth) return true
  const token = sessionStorage.getItem("token")
  if (!token) {
    return { name: "login", query: { redirect: to.fullPath } }
  }
  return true
})

export default router
