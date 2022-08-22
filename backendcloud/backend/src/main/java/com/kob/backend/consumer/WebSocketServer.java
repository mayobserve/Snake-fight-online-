package com.kob.backend.consumer;
import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")
// 映射到的链接
public class WebSocketServer {
    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    //线程安全的哈希map，UserId映射到对应链接
    //static 相当于对所有实例都可见
    private User user;
    private Session session = null;//每个链接由session维护
    private Game game = null;//创建game成员变量，在下实例化(包括地图，行进路线)
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";
    //基于matchingcontroller中的ip设置
    private static RestTemplate restTemplate;
    private static UserMapper userMapper;
    //注入数据库组件以便查询，由于websocket是特殊组件,写法不同
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        //加到set函数上
        WebSocketServer.userMapper = userMapper;
    }
    public static RecordMapper recordMapper;
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }
    @Autowired//本质是字符串处理
    //这个接口（service）是否有一个唯一的函数与之对应，且加了注解bean
    //有的话就调用他
    public void setRestTemplate (RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("connected!");
        Integer userId = JwtAuthentication.getUserId(token);
        //取出用户id
        this.user = userMapper.selectById(userId);
        //查找数据库对象
        if(this.user != null){//如果对象存在
            users.put(userId, this);
            //在map建立映射
        } else{//否则关闭连接
            this.session.close();
        }
        System.out.println(users);
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected");
        if(this.user != null) {
            users.remove(this.user.getId());
            //清楚就是消除映射
        }
    }

    public static void startGame(Integer aId, Integer bId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);

        Game game = new Game(13, 13, 20, a.getId(), b.getId());
        game.createMap();
        if(users.get(a.getId()) != null)
            //防止玩家中途退出，这样接收不到前端，就会退出匹配池
            users.get(a.getId()).game = game;
        if(users.get(b.getId()) != null)
            users.get(b.getId()).game = game;
        game.start();//执行新的线程，即一局比赛一个线程

        JSONObject respGame = new JSONObject();//将公用信息进行封装
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        JSONObject respA = new JSONObject();
        //将需要发送给用户A的信息 用键值对封装
        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game",respGame);//返回游戏公用信息
        if(users.get(a.getId()) != null)
            users.get(a.getId()).sendMessage(respA.toJSONString());
        //根据id 发送信息

        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if(users.get(b.getId()) != null)
            users.get(b.getId()).sendMessage(respB.toJSONString());
        //注意别写错
    }
    private void startMatching() {
        System.out.println("start matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMatching() {
        System.out.println("stop matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(int direction) {
        if(game.getPlayerA().getId().equals(user.getId())){
            game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            //考虑一些网络波动情况，也有可能来的是B，因此补全2种情况
            //其他的包就扔掉
            game.setNextStepB(direction);
        }
    }

    @OnMessage// 从Client接收消息
    public void onMessage(String message, Session session) {
        //类似路由/controller，将信息发往不同的接口处理
        System.out.println("receive message");
        JSONObject data = JSONObject.parseObject(message);
        //解析出来
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching();
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message){
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
                //后端基于当前url/session发送信息
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
