package com.websocket.Tasks;


import com.websocket.Service.WebSocketService;
import com.websocket.model.Player;
import com.websocket.model.PlayerMessage;
import com.websocket.model.PlayerStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.websocket.controller.PlayerController.playerQueue;
import static com.websocket.controller.PlayerController.nowPlayer;
import static com.websocket.controller.PlayerController.onlinePlayerList;

@Slf4j
@EnableScheduling
@Component
public class RefereeTask {

    @Autowired
    WebSocketService webSocketService;

    private static long lastWaitTime = 0;
    private static boolean waitNwePlayer = false;
    private static long waitNwePlayerTime = 0;
    private static int waitNwePlayerInterval = 5;  // wait interval

    private Map<String, String> resultMap = new HashMap<String, String>() {{
        put("rock,paper", "lost,win");
        put("rock,scissors", "win,lost");
        put("paper,scissors", "lost,win");
        put("paper,rock", "win,lost");
        put("scissors,rock", "lost,win");
        put("scissors,paper", "win,lost");
    }};

    @Scheduled(fixedRate = 2000)
    public void CheckOnlinePlayer() {
        int onlinePlayer = onlinePlayerList.size();
//        log.info("online player: {}", onlinePlayer);
        PlayerMessage playerMessage = new PlayerMessage();
        playerMessage.setSender("referee");
        playerMessage.setType(PlayerMessage.MessageType.ONLINE);
        playerMessage.setContent(String.valueOf(onlinePlayer));
        webSocketService.sendMsg(playerMessage);
    }


    @Scheduled(fixedRate = 1000)
    public void Judge() throws InterruptedException {
        if (nowPlayer.size() < 2) {
            if (waitNwePlayer) {
                if (((System.currentTimeMillis() / 1000) - waitNwePlayerTime) >= waitNwePlayerInterval) {
                    log.info("game over");
                    if (nowPlayer.size() > 0) {
                        Player winPlayer = nowPlayer.get(0);
                        PlayerMessage playerMessage = new PlayerMessage();
                        playerMessage.setSender("referee");
                        playerMessage.setType(PlayerMessage.MessageType.END);
                        playerMessage.setContent("win");
                        webSocketService.sendToClient(winPlayer.getSessionId(), playerMessage);
                        nowPlayer.remove(0);

                        playerMessage.setType(PlayerMessage.MessageType.NOTICE);
                        playerMessage.setContent(winPlayer.getName() + " is the final winner");
                        webSocketService.sendMsg(playerMessage);
                    }
                    waitNwePlayer = false;
                }
            }
            if (playerQueue.size() > 0) {
                nowPlayer.add(playerQueue.poll());
                log.info("add one player");
            }

//            if (lastWaitTime == 0 || ((System.currentTimeMillis() - lastWaitTime)/1000 > 30)) {
//                PlayerMessage playerMessage = new PlayerMessage();
//                playerMessage.setSender("referee");
//                playerMessage.setType(PlayerMessage.MessageType.NOTICE);
//                playerMessage.setContent("wait new player");
//                webSocketService.sendMsg(playerMessage);
//                lastWaitTime = System.currentTimeMillis();
//            }
        } else {
            List<String> vser = new ArrayList<>();
            for (int i = 0; i < nowPlayer.size(); i++) {
                Player player = nowPlayer.get(i);
                PlayerMessage playerMessage = new PlayerMessage();
                playerMessage.setSender("referee");
                String sessionId = player.getSessionId();
                String name = player.getName();
                vser.add(name);

                PlayerMessage pm = new PlayerMessage();
                pm.setSender("referee");
                pm.setType(PlayerMessage.MessageType.NOTICE);
                pm.setContent("### game begin ###");
                webSocketService.sendToClient(sessionId, pm);
            }
            PlayerMessage vsm = new PlayerMessage();
            vsm.setSender("referee");
            vsm.setType(PlayerMessage.MessageType.NOTICE);
            vsm.setContent(String.join(" vs ", vser));
            webSocketService.sendMsg(vsm);
            Player player1 = nowPlayer.get(0);
            Player player2 = nowPlayer.get(1);
            boolean timeout = false;
            boolean finish = false;
            int j = 60;
            while (j > 0) {
                if (!onlinePlayerList.contains(player1.getSessionId()) || !onlinePlayerList.contains(player2.getSessionId())) {
                    // at least one player offline
                    if (!onlinePlayerList.contains(player1.getSessionId()) && !onlinePlayerList.contains(player2.getSessionId())) {
                        vsm.setContent(player1.getName() + " and " +
                                player2.getName() + " left, no result");
                        webSocketService.sendMsg(vsm);
                        nowPlayer.clear();
                        break;
                    } else if (!onlinePlayerList.contains(player1.getSessionId())) {
                        vsm.setContent(player1.getName() + " left, " +
                                player2.getName() + " win");
                        webSocketService.sendMsg(vsm);

                        nowPlayer.remove(0);
                        player2.setGesture(null);
                        vsm.setType(PlayerMessage.MessageType.CONTINUE);
                        vsm.setContent("win");
                        webSocketService.sendToClient(player2.getSessionId(), vsm);
                        waitNwePlayer = true;
                        waitNwePlayerTime = System.currentTimeMillis() / 1000;
                        break;
                    } else {
                        vsm.setContent(player2.getName() + " left, " +
                                player1.getName() + " win");
                        webSocketService.sendMsg(vsm);

                        nowPlayer.remove(1);
                        player1.setGesture(null);
                        vsm.setType(PlayerMessage.MessageType.CONTINUE);
                        vsm.setContent("win");
                        webSocketService.sendToClient(player1.getSessionId(), vsm);
                        waitNwePlayer = true;
                        waitNwePlayerTime = System.currentTimeMillis() / 1000;
                        break;
                    }
                }
                if (player1.getGesture() != null && player2.getGesture() != null) {
                    if (player1.getGesture().equals(player2.getGesture())) {
                        log.info("end in a draw");
                        vsm.setContent("end in a draw");
                        webSocketService.sendMsg(vsm);
                        player1.setGesture(null);
                        player2.setGesture(null);
                        Thread.sleep(3000);
                        break;
                    } else {
                        String result = resultMap.get(player1.getGesture() + "," + player2.getGesture());
                        player1.setStatus(result.split(",")[0]);
                        player2.setStatus(result.split(",")[1]);
                        vsm.setContent(player1.getName() + " " + player1.getStatus() + ", " +
                        player2.getName() + " " + player2.getStatus());
                        webSocketService.sendMsg(vsm);

                        if (player1.getStatus().equals("lost")) {
                            vsm.setType(PlayerMessage.MessageType.END);
                            vsm.setContent("lost");
                            webSocketService.sendToClient(player1.getSessionId(), vsm);
                            nowPlayer.remove(0);

                            player2.setGesture(null);
                            vsm.setType(PlayerMessage.MessageType.CONTINUE);
                            vsm.setContent("win");
                            webSocketService.sendToClient(player2.getSessionId(), vsm);
                        } else {
                            vsm.setType(PlayerMessage.MessageType.END);
                            vsm.setContent("lost");
                            webSocketService.sendToClient(player2.getSessionId(), vsm);
                            nowPlayer.remove(1);

                            player1.setGesture(null);
                            vsm.setType(PlayerMessage.MessageType.CONTINUE);
                            vsm.setContent("win");
                            webSocketService.sendToClient(player1.getSessionId(), vsm);
                        }
                        waitNwePlayer = true;
                        waitNwePlayerTime = System.currentTimeMillis() / 1000;
                        break;
                    }
                }
                Thread.sleep(1000);
                j --;
            }
        }
    }

}
