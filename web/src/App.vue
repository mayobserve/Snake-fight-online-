<template>
  <NavBar></NavBar>
  <router-view></router-view>
  <!-- 自动根据网站变化，这个少了，跳转页面会出问题 -->
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

export default{
  components:{
    NavBar,
  },
}
</script>


<style>
body{
  background-image: url("./assets/background.jpg");
  background-size: cover;

}
</style>
