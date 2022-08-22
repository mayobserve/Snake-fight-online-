import { AcGameObject } from "./AcGameObject";

import { Snake } from "./Snack";
import { Wall } from "./Wall";
//不加括号 ，表明会在export default扔出去，相当于公共函数
//加括号，则用在export扔出去

//可以引入一个js
//当成继承的父类，也可以当作一个对象/变量类型（结构体）
export class GameMap extends AcGameObject{
    constructor(ctx, parent, store) {//画布，画布父元素
        super();

        this.ctx = ctx;
        this.parent = parent;
        this.L = 0;//格子的绝对距离

        this.store = store;

        this.rows = 13;//横纵格子的数量
        this.cols = 13;

        this.inner_walls_count = 20;//障碍物数量
        this.walls = [];//将渲染的格子存到数组中

        this.snakes = [
            new Snake({id: 0, color: '#4876EC', r:this.rows - 2, c:1}, this),
            new Snake({id: 1, color: '#F94848', r:1, c:this.cols - 2}, this),
        ]
    }

    creat_walls() {
        const g = this.store.state.pk.gamemap;
        //注意渲染放在最后
        for(let r = 0; r < this.rows; r++) {
            for(let c = 0; c < this.cols; c++) {
                if(g[r][c]) {
                    // new Wall(r, c, this)
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }  
    }

    
    add_listening_events() {//监听键盘输入
        this.ctx.canvas.focus();//聚焦方便读取输入

        this.ctx.canvas.addEventListener("keydown", e => {
            let d = -1;//定义方位
            if(e.key === 'w') d = 0;
            else if(e.key === 'd') d = 1;
            else if(e.key === 's') d = 2;
            else if(e.key === 'a') d = 3;
            
            
            if(d >= 0) {//发送移动指令
                this.store.state.pk.socket.send(JSON.stringify({
                    event: "move",
                    direction: d,
                }))
            }
            // else if(e.key === 'ArrowUp') snake1.set_direction(0);
            // else if(e.key === 'ArrowRight') snake1.set_direction(1);
            // else if(e.key === 'ArrowDown') snake1.set_direction(2);
            // else if(e.key === 'ArrowLeft') snake1.set_direction(3);
        });
    }

    start() {
        this.creat_walls();
        // for(let i = 0; i < 1000; i++ )//尝试1000次，保证联通
        //     if(this.creat_walls()) break;
        this.add_listening_events();
    }


    update_size(){
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        //格子的宽度为    div边框的宽高 除以 横纵格子的数量
        this.ctx.canvas.width = this.L * this.cols;
        //格子的宽度   格子乘横纵格子宽度
        this.ctx.canvas.height = this.L * this.rows;
    }

    check_ready(){//保证回合制，处于静止且下达（移动）指令才移动
        for(const snake of this.snakes){
            if(snake.status !== "idle") return false;
            if(snake.direction === -1) return false;
        }
        return true;
    }
    next_step() {//进入下一回合
        for(const snake of this.snakes) {
            snake.next_step();
            //snake是利用Snack创建的对象
            //调用Snack.js下的nextstep函数
        }
    }

    check_valid(cell) {//碰撞检测
        for (const wall of this.walls) {
            if(wall.r === cell.r && wall.c === cell.c)
            //横纵坐标恰好都符合
                return false;
        }

        for(const snake of this.snakes) {
            let k = snake.cells.length;
            if(!snake.check_tail_increasing()) {
                //蛇尾会动的时候，不判断蛇尾（将数组下标减少）
                k--;
            }
            for(let i = 0; i < k; i++){//撞到身体
                if(snake.cells[i].r === cell.r && snake.cells[i].c === cell.c)
                    return false;
            }
        }
        return true;
    }
    
    update() {
        this.update_size();
        if(this.check_ready()) {//双方下完指令后，进行图像移动
            this.next_step();
        }
        this.render();
    }

    render() {
        // this.ctx.fillStyle ='green';//填充色
        // this.ctx.fillRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);
        // //填充的位置及大小
        const color_even = "#AAD751", color_odd = "#A2D149";
        for(let r = 0; r < this.rows; r++){
            for(let l = 0; l < this.cols; l++){
                if((r + l) % 2 == 0){
                    this.ctx.fillStyle = color_even;//准备的颜色
                }else {
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(l * this.L, r * this.L, this.L, this.L);
                //填充（位置×格子大小）
            }
        }
    }
}