package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Record;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private final int[][] g;
    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    private final  Player playerA, playerB;
    //直接写这个，而未生成实例会报错，无视就行，下面会new出来

    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private ReentrantLock lock = new ReentrantLock();
    //锁是针对 nextStepA、nextStepB
    private String status = "playing";//判定当前游戏状态
    private String loser = ""; //all:平局 A：A输 B：B输

    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Integer idB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        playerA = new Player(idA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, 1, rows - 2, new ArrayList<>());
    }

    public  Player getPlayerA(){
        return playerA;
    }
    public  Player getPlayerB(){
        return playerB;
    }

    //存储两名玩家的下一步路径
    //且 操作之前先锁住，操作完（修改还数据）才能解锁
    public void setNextStepA(Integer nextStepA){
        lock.lock();
        try{
            this.nextStepA = nextStepA;
        } finally {//这样写 即使报异常 也不会死锁
            lock.unlock();
        }
    }
    public void setNextStepB(Integer nextStepB){
        lock.lock();
        try{
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }
    public int[][] getG() {
        return g;
    }

    private boolean check_connectivity(int sx, int sy, int tx,int ty) {
        if(sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for(int i = 0; i < 4;i ++){
            int x = sx + dx[i], y = sy + dy[i];
            if(x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                //java要对边界判断
                if(check_connectivity(x, y, tx, ty)){
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }
        g[sx][sy] = 0;
        return false;
    }
    private boolean draw(){
        for(int i = 0; i < this.rows; i++) {
            for(int j = 0; j < this.rows; j++){
                g[i][j] = 0;//0表示无墙
            }
        }

        //给左右两边作记号（打墙）
        for(int r = 0; r < this.rows; r++) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for(int c = 0; c < this.rows; c++) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        Random random = new Random();//随机生成墙体
        for(int i = 0; i < this.inner_walls_count / 2; i++){
            //1次涂色2个格子，所以/2
            for(int j = 0; j < 1000; j++) {
                //涂色1000次，从而保证一定能涂出符合数量的障碍
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if(g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1)
                    continue;//若这个位置已经true（被涂过色）就略过
                if(g[r][c] == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2)
                    continue;//留出2个对角的空位

                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }
        //验证连通性后返回
        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    public void createMap() {
        for(int i = 0; i < 1000; i++) {//尝试1000次，保证联通
            if(draw()) break;
        }
    }

    private boolean nextStep(){//等待两名的操作
        try {
            Thread.sleep(200);//为了适应前端
            //由于200ms画一格，因此每次停顿一下，再考虑数据是否读入
            // 睡眠要注意搭配抛异常
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0; i < 500; i++){
            try {
                Thread.sleep(100);//等待0.1s输入 且重复500次
                //参数：服务器延迟
                lock.lock();
                try{
                    if(nextStepA != null && nextStepB != null){//已读到2个玩家输入
                        playerA.getSteps().add(nextStepA);//返回
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                }finally {
                    lock.unlock();
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);//取出最后一格 判断是否为墙
        if(g[cell.x][cell.y] == 1) return false;
        for(int i = 0; i < n - 1; i++) {
            if(cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y)//是否与自身身体重合
                return false;
        }
        for(int i = 0; i < n - 1; i++) {
            if(cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y)
                return false;
        }
        return true;
    }
    private void judge() {//判断操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();
        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if(!validA || !validB){
            status = "finished";
            if(!validA && !validB) {
                loser = "all";
            }else if(!validA) {
                loser = "A";
            }else {
                loser = "B";
            }
        }

    }
    private void sendAllMessage(String message){
        //向前端（由于有2个用户，因此都发）发送消息
        if(WebSocketServer.users.get(playerA.getId()) != null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        //用WebSocketServer服务下的user（map）基于id，调出对应用户url，发送消息
        if(WebSocketServer.users.get(playerB.getId()) != null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }
    private void sendMove(){ //向客户端发送 移动信息
        lock.lock();//相应的要访问 nextStepA 就要锁
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            System.out.println("A " + nextStepA);
            resp.put("b_direction", nextStepB);
            System.out.println("B " + nextStepB);
            sendAllMessage(resp.toJSONString());//用接口发送信息
            nextStepA = nextStepB = null;//存完包 然后将其初始化
        }finally {
            lock.unlock();
        }
    }
    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < rows; j++) {
                res.append(g[i][j]);
            }
        }
        return  res.toString();
    }
    private void saveToDatabase(){
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
                );
        WebSocketServer.recordMapper.insert(record);
    }
    private void sendResult() { //服务器处理后向客户端发送 比赛结果
//        judge();
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    @Override
    public void run() {
//        System.out.println("run " + status);
        for(int i = 0; i < 1000; i++) {//13*13的地图*4个方向，因此调用1000次
            if(nextStep()){//已有下一把操作
                judge();
//                System.out.println(status);
                if(status.equals("playing")) {
                    sendMove();
                }else {
                    sendResult();
                    break;
                }
            }else {//没有操作 说明又玩家操作超时，判输
                status = "finished";
                lock.lock();
                try{
                    if(nextStepA == null && nextStepB == null) {
                        loser = "all";
                    }else if(nextStepA == null){
                        loser = "A";
                    }else{
                        loser = "B";
                    }
                }finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }
}
