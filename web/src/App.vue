<template>
  <div>
    <div>bot昵称：{{bot_name}}</div>
    <div>bot战力：{{bot_rating}}</div>
  </div>
  <router-view/>
</template>

<script>
import $ from 'jquery';
import { ref } from 'vue';


export default{
  name: "App",
  setup: ()=>{//函数入口
    let bot_name = ref("");
    let bot_rating = ref("");

    $.ajax({
      url: "http://127.0.0.1:3000/pk/getbotinfo/",
      type:"get",
      success(resp){
        console.log(resp);
        bot_name.value = resp.name;//注意是value ，？？？为什么
        
        bot_rating.value = resp.rating;
      }
    });

    return {
      bot_name,
      bot_rating,
    }
  }
  
}

</script>


<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
}

nav a.router-link-exact-active {
  color: #42b983;
}
</style>
