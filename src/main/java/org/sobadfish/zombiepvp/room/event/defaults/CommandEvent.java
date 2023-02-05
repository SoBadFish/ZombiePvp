package org.sobadfish.zombiepvp.room.event.defaults;

import cn.nukkit.Server;
import cn.nukkit.command.ConsoleCommandSender;

import org.sobadfish.zombiepvp.player.PlayerInfo;
import org.sobadfish.zombiepvp.room.GameRoom;
import org.sobadfish.zombiepvp.room.config.GameRoomEventConfig;
import org.sobadfish.zombiepvp.room.event.IGameRoomEvent;

/**
 * @author Sobadfish
 */
public class CommandEvent extends IGameRoomEvent {

    public CommandEvent(GameRoomEventConfig.GameRoomEventItem item) {
        super(item);
    }

    @Override
    public void onStart(GameRoom room) {
        for(PlayerInfo info: room.getLivePlayers()){
            Server.getInstance().getCommandMap().dispatch(new ConsoleCommandSender(),getEventItem().value.toString().replace("@p","'"+info.getName()+"'"));
        }
    }
}
