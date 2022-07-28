<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <!-- light是颜色 -->
  <div class="container">
    <!-- fluid是宽 -->
    <a class="navbar-brand" href="#">King of Bot</a>
    <div class="collapse navbar-collapse" id="navbarText">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        <li class="nav-item">
          <a :class= "route_name == 'pk_index' ? 'nav-link active' : 'nav-link' " 
          aria-current="page" href="/pk/">对战</a>
          <!-- <router-link class="nav-link" :to = "{name: 'home'}">对战</router-link> -->
          <!-- class前加冒号会作为表达式判断（即v-bind简写）
          'pk_index'即一个路径的名称
          active是默认的聚焦 -->
        </li>
        <li class="nav-item">
          <a :class= "route_name == 'record_index' ? 'nav-link active' : 'nav-link' " 
          href="/record/">对局列表</a>
        </li>
        <li class="nav-item">
          <a :class= "route_name == 'ranklist_index' ? 'nav-link active' : 'nav-link' " 
          href="/ranklist/">排行榜</a>
          <!-- 也可用绝对路径http://127.0.0.1:8081/ranklist/ -->
        </li>
      </ul>

      <ul class="navbar-nav">
        <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            V
            </a>
            <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                <li><a class="dropdown-item" href="/user/bot/">my bot</a></li>

                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#">exit</a></li>
            </ul>
        </li>
        
      </ul>

    </div>
  </div>
</nav>
</template>

<script>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

export default{
  setup(){
    const route = useRoute();
    //取出route
    let route_name = computed(() => route.name);
    //实时返回route的某个状态
    return {
      route_name,
    }
  },
  
}

</script>


<style scoped>

</style>
