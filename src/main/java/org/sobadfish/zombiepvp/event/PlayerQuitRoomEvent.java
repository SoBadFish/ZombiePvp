package org.sobadfish.zombiepvp.event;

import cn.nukkit.plugin.Plugin;
import org.sobadfish.zombiepvp.player.PlayerInfo;
import org.sobadfish.zombiepvp.room.GameRoom;


/**
 *
 * 玩家离开房间事件
 * @author SoBadFish
 * 2022/1/15
 */
public class PlayerQuitRoomEvent extends PlayerRoomInfoEvent {

    public boolean performCommand = true;

    public PlayerQuitRoomEvent(PlayerInfo playerInfo, GameRoom room, Plugin plugin) {
        super(playerInfo, room, plugin);
    }

    public void setPerformCommand(boolean performCommand) {
        this.performCommand = performCommand;
    }

    public boolean isPerformCommand() {
        return performCommand;
    }
}
