<template>
    <div class = "matchground">
        <div class="row">
            <div class="col-6">
                <div class = "user-photo">
                    <img :src="$store.state.user.photo" alt="">
                </div>  
                <div class = "user-username">
                    {{$store.state.user.username}}
                </div>
            </div>
            <div class="col-6">
                <div class = "user-photo">
                    <img :src="$store.state.pk.opponent_photo" alt="">
                </div>  
                <div class = "user-username">
                    {{$store.state.pk.opponent_username}}
                </div>
            </div>                
            <div class = "col-12" style = "text-align: center; padding-top: 10vh;">
                <button @click = "click_match_btn" type="button" class="btn btn-info btn-lg">{{ match_btn}}</button>
            </div>
        </div>
    </div>
</template>

<script>
import {ref} from 'vue'
import { useStore } from 'vuex';

export default {
    setup(){
        const store = useStore();//别忘了用vuex 要先 生成实例
        let match_btn = ref("匹配");

        const click_match_btn = ()=> {
            if(match_btn.value === "匹配"){
                //不要忘了ref要取value
                match_btn.value = "取消";
                store.state.pk.socket.send(JSON.stringify({
                //json封装成字符串
                //将发的数据包（字符串）作标记来区分不同的状态
                    event: "start-matching",
                }))
            }else{
                match_btn.value = "匹配";
                store.state.pk.socket.send(JSON.stringify({
                    event: "stop-matching",
                }))
            }
        }
        return {
            match_btn,
            click_match_btn
        }
    }
     
}

</script>


<style scoped>
div.matchground {
    width: 60vw;
    height: 70vh;
    background-color: rgba(50, 50, 50, 0.5);
    margin: 40px auto;
}
div.user-photo {
    text-align: center;
    padding-top: 10vh;
}
div.user-photo > img {
    border-radius: 50%;
    width: 20vh;
}
div.user-username {
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    color: white;
    padding-top: 2vh;
}
</style>
