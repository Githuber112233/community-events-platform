import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './index.css'

// 路由懒加载
const Home = () => import('./pages/Home.vue')
const Events = () => import('./pages/Events.vue')
const EventDetail = () => import('./pages/EventDetail.vue')
const Profile = () => import('./pages/Profile.vue')
const CreateEvent = () => import('./pages/CreateEvent.vue')
const EditEvent = () => import('./pages/EditEvent.vue')
const Login = () => import('./pages/Login.vue')
const Register = () => import('./pages/Register.vue')
const Admin = () => import('./pages/Admin.vue')
const Layout = () => import('./components/Layout.vue')
const FriendsList = () => import('./pages/FriendsList.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', component: Home },
        { path: 'events', component: Events },
        { path: 'events/:id', component: EventDetail },
        { path: 'profile', component: Profile },
        { path: 'profile/:id', component: Profile },
        { path: 'create-event', component: CreateEvent },
        { path: 'edit-event/:id', component: EditEvent },
        { path: 'friends/:type/:userId?', component: FriendsList },
      ],
    },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/admin', component: Admin },
  ],
})

const app = createApp(App)
app.use(router)
app.mount('#app')
