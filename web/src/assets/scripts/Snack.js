import { AcGameObject } from "./AcGameObject";
import { Cell } from "./Cell";

export class Snake extends AcGameObject{
    //export default 就不用对引入文件加{}
    constructor(info, gamemap){
        //类似地图，要把1.绘制的基本信息（颜色。id） 
        //2.上层蒙版/父元素（即gamemap引入）

    //预设信息
        super();
        this.id = info.id;
        this.color = info.color;
        this.gamemap = gamemap;

    //渲染相关
        this.cells = [new Cell(info.r, info.c)];
        //0位置存放蛇头，其余存放蛇身体
        //左下角的蛇初始朝上，右下角朝下
        this.eye_direction = 0;
        if(this.id === 1) 
            this.eye_direction = 2;
        this.eye_dx = [//蛇 2只眼睛 x轴位置
            [-1, 1],
            [1, 1],
            [1, -1],
            [-1, -1],
        ];
        this.eye_dy = [
            [-1, -1],
            [-1, 1],
            [1, 1],
            [-1, 1],
        ]

    //移动模块
        this.next_cell = null;//下一步的位置
        this.speed = 5;//蛇速度 每秒走5格子
        this.direction = -1;//-1无指令，0，1，2，3表示上右下左、
        this.dr = [-1, 0, 1, 0];
        this.dc = [0, 1, 0, -1];
    //状态机：idle静止，move移动，die死亡
        this.status = "idle";

        this.step = 0;//回合数
        this.eps = 1e-2;//允许的误差
    }

    start(){

    }

    set_direction(d) {
        this.direction = d;//将键盘输入传给direction
    }

    check_tail_increasing() {//判断蛇尾是否变长
        if(this.step <= 10) return true;
        if(this.step % 3 === 1) return true;
        return false;
    }
    next_step() {//蛇的下一步
        const d = this.direction;//提取出方向，从而利用方向数组计算位置
        this.next_cell = new Cell(this.cells[0].r + this.dr[d], this.cells[0].c + this.dc[d]);
        //坐标和中心点
        this.eye_direction = d;

        this.direction = -1;//清空操作
        this.status = "move";
        this.step ++;

        const k = this.cells.length;
        for(let i = k; i > 0; i --){
           this.cells[i] = JSON.parse(JSON.stringify(this.cells[i - 1]));
           //而不是this.cells[i] = this.cells[i - 1]
           //每个元素向后移动，然后之前害创建了一个蛇头（此时叫下一个点），这样蛇就变长了
        }

    }

    update_move(){
        //实现能够斜向移动
        const dx = this.next_cell.x - this.cells[0].x;
        const dy = this.next_cell.y - this.cells[0].y;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if(distance < this.eps) {//小于误差，就移动
            this.cells[0] = this.next_cell;
            this.next_cell = null;
            this.status = "idle";

            if(!this.check_tail_increasing()) {
                //移动完，发现蛇尾无需变长，就扔出末尾元素
                this.cells.pop();
            }
        } else{//不重合，就移动
            const move_distance = this.speed * this.timedelta / 1000;
            //每两帧之间走的距离
            this.cells[0].x += move_distance * dx / distance;
            this.cells[0].y += move_distance * dy / distance;
            //偏量移动

            if(!this.check_tail_increasing()){//蛇尾移动
                const k = this.cells.length;
                const tail = this.cells[k - 1], tail_target = this.cells[k - 2];
                //最后一个tail ，和倒二元素tail_target是目标
                const tail_dx = tail_target.x - tail.x;
                const tail_dy = tail_target.y - tail.y;
                tail.x += move_distance * tail_dx / distance;
                tail.y += move_distance * tail_dy / distance;
            }
        }
    }

    

    

    update(){
        if(this.status === 'move') {
            this.update_move();
        }
        this.render();
        
    }

    render(){
        const L = this.gamemap.L;//圆半径
        const ctx = this.gamemap.ctx;//获取画布

        //绘制圆形
        ctx.fillStyle = this.color;

        if(this.status === "die"){
            ctx.fillStyle = "white";
            //将死亡状态显示出来
        }


        for(const cell of this.cells){
            ctx.beginPath();//创建路径起始点
            ctx.arc(cell.x * L, cell.y * L, L / 2 * 0.8, 0, Math.PI * 2);
            //圆的起止点，单元格距离，半径，起始和中止角度
            ctx.fill();//封闭图形填充颜色
        }

        for(let i = 1; i < this.cells.length; i++) {
            const a = this.cells[i - 1], b = this.cells[i];
            if(Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps)
                continue;
            if(Math.abs(a.x - b.x) < this.eps) {//竖方向
                ctx.fillRect((a.x - 0.4) * L, Math.min(a.y, b.y) * L, L * 0.8, Math.abs(a.y - b.y) * L);
            //矩形的左上点，宽，高
            }else {
                ctx.fillRect(Math.min(a.x, b.x) * L, (a.y - 0.4) * L, Math.abs(a.x - b.x) * L, L * 0.8 );
            }
        }

        ctx.fillStyle = "black";
        for(let i = 0; i < 2; i++) {
            const eye_x = (this.cells[0].x + this.eye_dx[this.eye_direction][i] * 0.15) * L;
            const eye_y = (this.cells[0].y + this.eye_dy[this.eye_direction][i] * 0.15) * L;

            ctx.beginPath();
            ctx.arc(eye_x, eye_y, L * 0.05, 0, Math.PI * 2);
            ctx.fill();
        }

    }
}