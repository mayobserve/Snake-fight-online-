package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor//有参构造函数
@NoArgsConstructor//无参构造函数
public class Player {//每个玩家的信息
    private Integer id;
    private Integer sx, sy;//起点坐标
    private List<Integer> steps;//路径序列

    private boolean check_tail_increasing(int step) {
        if(step <= 10) return true;//小于等于10直接增长
        return  step % 3 == 1;//大于10，根据每3步+1增长
    }

    public List<Cell> getCells() {
        List<Cell> res = new ArrayList<>();//变长数组
        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));//初始蛇身体 信息
        for(int d : steps) {
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x,y));//根据移动情况，加入蛇身体
            if(!check_tail_increasing(++step)){//判断是否要变长
                res.remove(0);
            }
        }
        return res;
    }

    public String getStepsString() {
        StringBuilder res = new StringBuilder();
        for(int d : steps) {
            res.append(d);
        }
        return res.toString();
    }
}
