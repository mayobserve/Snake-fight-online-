<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <!-- light是颜色 -->
  <div class="container">
    <!-- fluid是宽 -->
    <a class="navbar-brand" href="#">King of Bot</a>
    <div class="collapse navbar-collapse" id="navbarText">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        <li class="nav-item">
          <router-link :class= "route_name == 'pk_index' ? 'nav-link active' : 'nav-link' " 
          aria-current="page" :to = "{name: 'pk_index'}">对战</router-link>
          <!-- <router-link class="nav-link" :to = "{name: 'home'}">对战</router-link> -->
          <!-- class前加冒号会作为表达式判断（即v-bind简写）
          'pk_index'即一个路径的名称
          active是默认的聚焦 -->
        </li>
        <li class="nav-item">
          <router-link :class= "route_name == 'record_index' ? 'nav-link active' : 'nav-link' " 
          :to = "{name: 'record_index'}">对局列表</router-link>
        </li>
        <li class="nav-item">
          <router-link :class= "route_name == 'ranklist_index' ? 'nav-link active' : 'nav-link' " 
          :to = "{name: 'ranklist_index'}">排行榜</router-link>
          <!-- 也可用绝对路径http://127.0.0.1:8081/ranklist/ -->
        </li>
      </ul>

      <ul class="navbar-nav" v-if="$store.state.user.is_login">
        <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            {{ $store.state.user.username }}
            </a>
            <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                <li><router-link class="dropdown-item" :to="{ name: 'user_bot_index'}">my bot</router-link></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" @click="logout">exit</a></li>
            </ul>
        </li>
      </ul>

      <ul class="navbar-nav" v-else-if = "!$store.state.user.pulling_info">
      <!-- 没拉取到信息，展示登陆，注册按钮 -->
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_login' }" role="button">
            登录
          </router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" :to="{name: 'user_account_register'}" role="button">
            注册
          </router-link>
        </li>
      </ul>

    </div>
  </div>
</nav>
</template>

<script>
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useStore } from 'vuex'

export default{
  setup(){
    const route = useRoute();
    //取出route
    const store =  useStore();
    
    let route_name = computed(() => route.name);
    //实时返回route的某个状态
    const logout = ()=> {
      store.dispatch("logout");
    }
    return {
      route_name,
      logout
    }
  },
}
</script>


<style scoped>

</style>
