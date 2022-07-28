const AC_GAME_OBJECTS = [];

export class AcGameObject {
    constructor(){
        AC_GAME_OBJECTS.push(this);//场景内每一个元素填入数组
        this.timedelta = 0;
        //用他来控制速度，即控制时间间隔（即相邻两帧时间间隔）
        this.has_called_start = false;
    }

    start() {//只执行一次（第一帧,因此需要用一个变量mark是否为第一帧）

    }
    update() {//除第一帧外，平时的执行

    }
    on_destory() {

    }


    destory() {//删除
        this.on_destory();
        for(let i in AC_GAME_OBJECTS){
            const obj = AC_GAME_OBJECTS[i];
            if(obj === this){
                AC_GAME_OBJECTS.splice(i);
                break
            }
        }
    }
}

let last_timestamp;//上一次执行的时间
const step = timestamp =>{//当前时刻
    for(let obj of AC_GAME_OBJECTS){//遍历所有的内容
        if(!obj.has_called_start){//第一次执行
            obj.has_called_start = true;//mark标记
            obj.start();//执行start
        }else{//后续执行updata
            obj.timedelta = timestamp - last_timestamp;//当前-上一次时刻 即间隔
            obj.update();
        }
    }

    last_timestamp = timestamp;
    requestAnimationFrame(step);
}
requestAnimationFrame(step);

