<template>
    <PlayGround v-if ="$store.state.pk.status === 'playing'"></PlayGround>
    <MatchGround v-if ="$store.state.pk.status === 'matching'"></MatchGround>
    <ResultBoard v-if ="$store.state.pk.loser != 'none'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import MatchGround from '../../components/MatchGround.vue'
//一个点就是往上一级
import ResultBoard from '../../components/ResultBoard.vue'
import { onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'

export default{
    components: {
        PlayGround,
        MatchGround,
        ResultBoard
    },
    setup() {
        const store = useStore();
        const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;
        //注 要采用 ``而不是单/双引号
        let socket = null;
        onMounted(() => {//当组件被打开时 执行

            store.commit("updateOpponent", {
                username:"我的对手",
                photo: "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic2.zhimg.com%2Fv2-0e157603c68b61a9810733ddee5afa2d_r.jpg&refer=http%3A%2F%2Fpic2.zhimg.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1663206223&t=33c6a24df14955f383fd53c79cc1499d"
            })
            socket = new WebSocket(socketUrl);
            //生成socket

            socket.onopen = () => {//建立连接
                console.log("connected");
                store.commit("updateSocket", socket);
            }

            socket.onmessage = msg => {//接收信息
                const data = JSON.parse(msg.data);
                if(data.event === "start-matching") {
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 2000);
                    store.commit("updateGamemap", data.game);
                }else if(data.event === "move"){
                    const game = store.state.pk.gameObject;
                    //从全局中取出gameObject的蛇信息
                    const [snake0, snake1] = game.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                }else if(data.event === "result") {
                    console.log(data);//前端渲染蛇死亡
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;

                    if(data.loser === "all" || data.loser === "A"){
                        snake0.status = "die";
                    }
                    if(data.loser === "all" || data.loser === "B"){
                        snake1.status = "die";
                    }
                    store.commit("updateLoser", data.loser);
                }
            }

            socket.onclose = () => {//关闭连接
                console.log("disconnected");
            }
        });
        onUnmounted(() => {//组件关闭时，socket关闭
            socket.close();
            //如果不断开，下次会再次增加新的链接
            store.commit("updateStatus", "matching");
        })

    }
}

</script>


<style scoped>
</style>
