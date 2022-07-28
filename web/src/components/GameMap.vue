<template>
    <div ref="parent" class = "gamemap">
        <!-- 用ref= 可以绑定相应的div -->
        <canvas ref="canvas" tabindex="0"></canvas>
    </div>
    <!-- <div>123131</div> -->
</template>

<script>
import { GameMap } from "@/assets/scripts/GameMap";
import { ref, onMounted} from 'vue'

export default {
    setup() {
        let parent = ref(null);
        let canvas = ref(null);

        onMounted(() => {//组件 初始运行的调用的函数
            new GameMap(canvas.value.getContext('2d'), parent.value);
            //canvas的值还需要用getcontext读取

        });

        return {
            parent,
            canvas,
        }
    }
}

</script>


<style scoped>
div.gamemap{
    /* 100是为了加上蒙版 */
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
