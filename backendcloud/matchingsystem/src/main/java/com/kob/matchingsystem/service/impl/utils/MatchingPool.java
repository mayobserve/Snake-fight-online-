package com.kob.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component//bean能够注入，加上Component注解，类变成Component
public class MatchingPool extends Thread {
    private static List<Player> players = new ArrayList<>();
    //由于有锁，因此不需要用线程安全的list
    //即这个锁 加给players
    private ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;//从而在线程内部进行http通信
    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        MatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId, Integer rating) {
        lock.lock();
        try {
            players.add(new Player(userId, rating, 0));
        }finally {
            lock.unlock();
        }
    }
    public void removePlayer(Integer userId) {
        lock.lock();
        try {//使用容器复制，来进行消除某名玩家
            List<Player> newPlayers = new ArrayList<>();
            for(Player player: players) {
                if(!player.getUseId().equals(userId)) {
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        }finally {
            lock.unlock();
        }
    }
    private void increasingWaitingTime() {//随时间 拓展匹配阈值
        for(Player player : players) {
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }

    private boolean checkMatched(Player a, Player b) {//判断是否匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        //考虑战力因素
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        //考虑等待时间影响
        return ratingDelta <= waitingTime * 10;
        //等待时间的权重是10
    }

    private void sendResult(Player a,Player b){//返回结果
        System.out.println("send result:" + a + " " + b);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUseId().toString());
        data.add("b_id", b.getUseId().toString());
        restTemplate.postForObject(startGameUrl,data,String.class);
        //第三个是返回值的类的反射

    }
    private void matchPlayers() {//匹配系统
        System.out.println("match players:" + players.toString());
        boolean[] used = new boolean[players.size()];
        for(int i = 0; i < players.size(); i++) {
            if(used[i]) continue;//如果以用过，则跳过该次循环
            for(int j = i + 1; j < players.size(); j++){
                if(used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if(checkMatched(a, b)) {//判断是否能匹配
                    used[i] = used[j] = true;//则标记用过
                    sendResult(a, b);
                    break;
                }
            }
        }
        //更新当前匹配池
        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i ++) {
            if(!used[i]) {//没用过就放入 list副本
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;//复制给list
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);//每隔1s进行匹配
                lock.lock();
                try {
                    increasingWaitingTime();
                    matchPlayers();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
