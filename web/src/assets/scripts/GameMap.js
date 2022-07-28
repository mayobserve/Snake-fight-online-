import { AcGameObject } from "./AcGameObject";

import { Snake } from "./Snack";
import { Wall } from "./Wall";
//不加括号 ，表明会在export default扔出去，相当于公共函数
//加括号，则用在export扔出去

//可以引入一个js
//当成继承的父类，也可以当作一个对象/变量类型（结构体）
export class GameMap extends AcGameObject{
    constructor(ctx, parent) {//画布，画布父元素
        super();

        this.ctx = ctx;
        this.parent = parent;
        this.L = 0;//格子的绝对距离

        this.rows = 13;//横纵格子的数量
        this.cols = 13;

        this.inner_walls_count = 20;//障碍物数量
        this.walls = [];//将渲染的格子存到数组中

        this.snakes = [
            new Snake({id: 0, color: '#4876EC', r:this.rows - 2, c:1}, this),
            new Snake({id: 1, color: '#F94848', r:1, c:this.cols - 2}, this),
        ]
    }

    check_connectivity(g, sx, sy, tx, ty){ //联通算法 floyd？？？
        if(sx === tx && sy === ty) return true;
        g[sx][sy] = true;
        
        let dx = [-1, 0, 1, 0], dy = [0, 1, 0, -1]; 
        for(let i = 0; i < 4; i++){
            let x = sx + dx[i], y = sy + dy[i];
            if(!g[x][y] && this.check_connectivity(g, x, y, tx, ty)) 
            //没撞墙 且 搜到终点
                return true;
        }
        return false;
    }


    creat_walls() {
        const g = [];
        for(let r = 0; r < this.rows; r++) {
            g[r] = [];//构造2维数组
            for(let c = 0; c < this.cols; c++) {
                g[r][c] = false;
            }
        }

        for(let r = 0; r < this.rows; r++) {
            g[r][0] = g[r][this.cols - 1] = true;
            //给左右两边作记号（打墙）
        }
        for(let c = 0; c < this.cols; c++) {
            g[0][c] = g[this.rows - 1][c] = true;
        }
        
        
        for(let i = 0; i < this.inner_walls_count / 2; i++ ){
            //1次涂色2个格子，所以/2
            for(let j = 0; j < 1000; j++){
                //涂色1000次，从而保证一定能涂出符合数量的障碍
                let r = parseInt(Math.random() * this.rows);//行
                let c = parseInt(Math.random() * this.cols);//列
                if(g[r][c] || g[c][r]) continue;
                //若这个位置已经true（被涂过色）就略过
                if(r === this.rows - 2 && c === 1 || r === 1 && c === this.cols - 2)
                    continue;
                //留出2个对角的空位
                g[r][c] = g[c][r] = true;
                break;
            }
        }
        //创建一个副本传送
        const copy_g = JSON.parse(JSON.stringify(g));
        //直接传状态，万一把状态修改了会引起bug
        //转化乘json，再把json解析
        //传当前地图状态，保存在g中
        if(!this.check_connectivity(copy_g, this.rows - 2, 1, 1, this.cols - 2))
        //起点和终点坐标
            return false;

        //注意渲染放在最后
        for(let r = 0; r < this.rows; r++) {
            for(let c = 0; c < this.cols; c++) {
                if(g[r][c]) {
                    // new Wall(r, c, this)
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }
        
        return true;
    }

    
    add_listening_events() {//监听键盘输入
        this.ctx.canvas.focus();//聚焦方便读取输入

        const [snake0, snake1] = this.snakes;//临时起别名
        this.ctx.canvas.addEventListener("keydown", e => {
            if(e.key === 'w') snake0.set_direction(0);
            else if(e.key === 'd') snake0.set_direction(1);
            else if(e.key === 's') snake0.set_direction(2);
            else if(e.key === 'a') snake0.set_direction(3);
            else if(e.key === 'ArrowUp') snake1.set_direction(0);
            else if(e.key === 'ArrowRight') snake1.set_direction(1);
            else if(e.key === 'ArrowDown') snake1.set_direction(2);
            else if(e.key === 'ArrowLeft') snake1.set_direction(3);
        });
    }

    start() {
        for(let i = 0; i < 1000; i++ )//尝试1000次，保证联通
            if(this.creat_walls()) break;
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