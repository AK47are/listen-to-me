import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/Home.vue'),
        },
        {
          path: 'audio/:id',
          name: 'AudioDetail',
          component: () => import('@/views/AudioDetail.vue'),
        },
        {
          path: 'favorite',
          name: 'Favorite',
          component: () => import('@/views/Favorite.vue'),
        },
        {
          path: 'like',
          name: 'Like',
          component: () => import('@/views/Like.vue'),
        },
        {
          path: 'history',
          name: 'History',
          component: () => import('@/views/History.vue'),
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
        },
        {
          path: 'wallet',
          name: 'Wallet',
          component: () => import('@/views/Wallet.vue'),
        },
        {
          path: 'creator/audio',
          name: 'CreatorAudio',
          component: () => import('@/views/CreatorAudio.vue'),
        },
        {
          path: 'creator/create',
          name: 'CreateAudio',
          component: () => import('@/views/CreateAudio.vue'),
        },
        {
          path: 'creator/slots',
          name: 'CreatorSlots',
          component: () => import('@/views/CreatorSlots.vue'),
        },
        {
          path: 'creator/consult',
          name: 'CreatorConsult',
          component: () => import('@/views/CreatorConsult.vue'),
        },
        {
          path: 'consult',
          name: 'CreatorList',
          component: () => import('@/views/CreatorList.vue'),
        },
        {
          path: 'consult/creator/:id',
          name: 'CreatorSlots',
          component: () => import('@/views/CreatorSlots.vue'),
        },
        {
          path: 'consult/book',
          name: 'ConsultBook',
          component: () => import('@/views/ConsultBook.vue'),
        },
        {
          path: 'user/consult',
          name: 'UserConsult',
          component: () => import('@/views/UserConsult.vue'),
        },
        {
          path: 'admin',
          name: 'AdminDashboard',
          component: () => import('@/views/AdminDashboard.vue'),
        },
      ],
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
    },
  ],
})

export default router
