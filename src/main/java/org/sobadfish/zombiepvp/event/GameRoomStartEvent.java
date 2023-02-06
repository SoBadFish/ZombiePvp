package org.sobadfish.zombiepvp.event;


import cn.nukkit.event.Cancellable;
import cn.nukkit.plugin.Plugin;
import org.sobadfish.zombiepvp.room.GameRoom;

/**
 * 房间游戏开始事件
 * @author SoBadFish
 * 2022/1/15
 */
public class GameRoomStartEvent extends GameRoomEvent implements Cancellable {

    public GameRoomStartEvent(GameRoom room, Plugin plugin) {
        super(room, plugin);
    }
}
