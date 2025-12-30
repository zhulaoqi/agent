import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('../views/ChatSimple.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/image-gen',
      name: 'ImageGen',
      component: () => import('../views/ImageGen.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/script',
      name: 'Script',
      component: () => import('../views/ScriptGen.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/naming',
      name: 'Naming',
      component: () => import('../views/NamingHelper.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/history',
      name: 'History',
      component: () => import('../views/History.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/workflow',
      name: 'Workflow',
      component: () => import('../views/WorkflowSimple.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/rag',
      name: 'RAG',
      component: () => import('../views/RagDemo.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/multi-agent',
      name: 'MultiAgent',
      component: () => import('../views/MultiAgentDemo.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/human-in-loop',
      name: 'HumanInLoop',
      component: () => import('../views/HumanInLoopDemo.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/framework',
      name: 'FrameworkCenter',
      component: () => import('../views/FrameworkCenter.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到主页
    next('/chat')
  } else {
    next()
  }
})

export default router

