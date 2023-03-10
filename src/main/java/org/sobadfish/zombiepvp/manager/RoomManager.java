package org.sobadfish.zombiepvp.manager;


import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockTNT;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityNameable;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.level.WeatherChangeEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemColorArmor;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.zombiepvp.entity.DamageFloatTextEntity;
import org.sobadfish.zombiepvp.entity.EntityTnt;
import org.sobadfish.zombiepvp.item.ItemIDSunName;
import org.sobadfish.zombiepvp.item.button.RoomQuitItem;
import org.sobadfish.zombiepvp.item.button.TeamChoseItem;
import org.sobadfish.zombiepvp.panel.ChestInventoryPanel;
import org.sobadfish.zombiepvp.panel.DisPlayWindowsFrom;
import org.sobadfish.zombiepvp.panel.from.GameFrom;
import org.sobadfish.zombiepvp.panel.from.button.BaseIButton;
import org.sobadfish.zombiepvp.panel.items.BasePlayPanelItemInstance;
import org.sobadfish.zombiepvp.panel.items.PlayerItem;
import org.sobadfish.zombiepvp.player.PlayerInfo;
import org.sobadfish.zombiepvp.player.team.TeamInfo;
import org.sobadfish.zombiepvp.room.GameRoom;
import org.sobadfish.zombiepvp.room.GameRoom.GameType;
import org.sobadfish.zombiepvp.room.config.GameRoomConfig;
import org.sobadfish.zombiepvp.room.config.ItemConfig;

import java.io.File;
import java.util.*;

/**
 * ?????????????????????
 * ???????????????????????????
 * ??????????????????
 *
 * @author Sobadfish
 * @date 2022/9/9
 */
public class RoomManager implements Listener {

    public Map<String, GameRoomConfig> roomConfig;

    public static LanguageManager language = TotalManager.getLanguage();

    public static List<GameRoomConfig> LOCK_GAME = new ArrayList<>();

    public LinkedHashMap<String,String> playerJoin = new LinkedHashMap<>();

    public Map<String, GameRoom> getRooms() {
        return rooms;
    }

    private Map<String, GameRoom> rooms = new LinkedHashMap<>();

    public boolean hasRoom(String room){
        return roomConfig.containsKey(room);
    }

    public boolean hasGameRoom(String room){
        return rooms.containsKey(room);
    }

    private RoomManager(Map<String, GameRoomConfig> roomConfig){
        this.roomConfig = roomConfig;
    }

    /**
     * ????????????????????????
     * @param level ??????
     * @return ????????????
     * */
    private GameRoom getGameRoomByLevel(Level level){
        for(GameRoom room : new ArrayList<>(rooms.values())){
            if(room.getRoomConfig().worldInfo.getGameWorld() == null){
                continue;
            }
            if(room.getRoomConfig().worldInfo.getGameWorld().getFolderName().equalsIgnoreCase(level.getFolderName())){
                return room;
            }
        }
        return null;
    }


    /**
     * ??????????????????????????????
     * @param player ???????????? {@link PlayerInfo}
     * @param roomName ????????????
     * @return ????????????????????????
     * */
    public boolean joinRoom(PlayerInfo player, String roomName){
        PlayerInfo info = getPlayerInfo(player.getPlayer());
        if(info != null){
            player = info;
        }

        if (hasRoom(roomName)) {
            if (!hasGameRoom(roomName)) {
                if(!enableRoom(getRoomConfig(roomName))){
                    player.sendForceMessage(language.getLanguage("room-enable-error","&c[1] ???????????????",roomName));
                    return false;
                }
            }else{
                GameRoom room =getRoom(roomName);
                if(room != null){
                    if(RoomManager.LOCK_GAME.contains(room.getRoomConfig()) && room.getType() == GameType.END || room.getType() == GameType.CLOSE){
                        player.sendForceMessage(language.getLanguage("room-enable-error","&c[1] ???????????????",roomName));
                        return false;
                    }
                    if(room.getWorldInfo().getConfig().getGameWorld() == null){
                        return false;
                    }
                    if(room.getType() == GameType.END ||room.getType() == GameType.CLOSE){
                        player.sendForceMessage(language.getLanguage("room-status-ending","&c[1] ?????????",roomName));
                        return false;
                    }
                }
            }

            GameRoom room = getRoom(roomName);
            if(room == null){
                return false;
            }
            switch (room.joinPlayerInfo(player,true)){
                case CAN_WATCH:
                    if(!room.getRoomConfig().hasWatch){
                        player.sendForceMessage(language.getLanguage("room-ban-watch","&c?????????????????????????????????"));
                    }else{

                        if(player.getGameRoom() != null && !player.isWatch()){
                            player.sendForceMessage(language.getLanguage("room-ban-watch-join","&c????????????????????????"));
                            return false;
                        }else{
                            room.joinWatch(player);
                            return true;
                        }
                    }
                    break;
                case NO_LEVEL:
                    player.sendForceMessage(language.getLanguage("room-level-resting","&c?????????????????????????????????????????????"));
                    break;
                case NO_ONLINE:
                    break;
                case NO_JOIN:
                    player.sendForceMessage(language.getLanguage("room-ban-join","&c????????????????????????"));
                    break;
                default:
                    //????????????
                    return true;
            }
        } else {
            player.sendForceMessage(language.getLanguage("room-absence","&c????????? &r[1] &c??????",roomName));

        }
        return false;
    }


    /**
     * ????????????
     * @param config ???????????? {@link GameRoomConfig}
     *
     * @return ????????????????????????
     * */
    public boolean enableRoom(GameRoomConfig config){
        if(config.getWorldInfo().getGameWorld() == null){
            return false;
        }
        if(!RoomManager.LOCK_GAME.contains(config)){
            RoomManager.LOCK_GAME.add(config);

            GameRoom room = GameRoom.enableRoom(config);
            if(room == null){
                RoomManager.LOCK_GAME.remove(config);
                return false;
            }
            rooms.put(config.getName(),room);
            return true;
        }else{

            return false;
        }

    }

    public GameRoomConfig getRoomConfig(String name){
        return roomConfig.getOrDefault(name,null);
    }

    public List<GameRoomConfig> getRoomConfigs(){
        return new ArrayList<>(roomConfig.values());
    }

    /**
     * ??????????????????????????????????????????
     * @param name ????????????
     * @return ????????????
     * */
    public GameRoom getRoom(String name){
        GameRoom room = rooms.getOrDefault(name,null);
        if(room == null || room.worldInfo == null){
            return null;
        }

        if(room.getWorldInfo().getConfig().getGameWorld() == null){
            return null;
        }
        return room;
    }

    /**
     * ??????????????????
     * @param name ????????????
     * */
    public void disEnableRoom(String name){
        if(rooms.containsKey(name)){
            rooms.get(name).onDisable();

        }
    }





    /**
     * ?????????????????????????????????
     * @param player ?????? {@link Player}
     * @return ???????????? {@link PlayerInfo}
     * */
    public PlayerInfo getPlayerInfo(EntityHuman player){
        //TODO ????????????????????????
        if(playerJoin.containsKey(player.getName())) {
            String roomName = playerJoin.get(player.getName());
            if (!"".equalsIgnoreCase(roomName)) {
                if (rooms.containsKey(roomName)) {
                    return rooms.get(roomName).getPlayerInfo(player);
                }
            }
        }else{
            if(player.namedTag.contains("room")){
                String roomName = player.namedTag.getString("room");
                if (!"".equalsIgnoreCase(roomName)) {
                    if (rooms.containsKey(roomName)) {
                        return rooms.get(roomName).getPlayerInfo(player);
                    }
                }
            }

        }
        return null;
    }



    /**
     * ????????????????????????
     * @param file ????????????
     * @return ???????????????
     * */
    public static RoomManager initGameRoomConfig(File file){
        Map<String, GameRoomConfig> map = new LinkedHashMap<>();
        if(file.isDirectory()){
            File[] dirNameList = file.listFiles();
            if(dirNameList != null && dirNameList.length > 0) {
                for (File nameFile : dirNameList) {
                    if(nameFile.isDirectory()){
                        String roomName = nameFile.getName();
                        GameRoomConfig roomConfig = GameRoomConfig.getGameRoomConfigByFile(roomName,nameFile);
                        if(roomConfig != null){
                            TotalManager.sendMessageToConsole(language.getLanguage("room-loading-success","&a???????????? [1] ??????",roomName));
                            map.put(roomName,roomConfig);

                        }else{
                            TotalManager.sendMessageToConsole(language.getLanguage("room-loading-error","&c???????????? [1] ??????",roomName));

                        }
                    }
                }
            }
        }
        return new RoomManager(map);
    }

    /*
     * ***********************************************
     *
     * ???????????? ????????????
     *
     * ***********************************************
     * */

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        //TODO ???????????? ??????
        Player player = event.getPlayer();
        if(playerJoin.containsKey(player.getName())){
            player.setFoodEnabled(false);
            player.setGamemode(2);
            player.setMaxHealth(20);
            player.setHealth(20);
            String room = playerJoin.get(player.getName());
            if(hasGameRoom(room)) {
                GameRoom room1 = getRoom(room);
                if (room1 == null) {
                    reset(player);
                    playerJoin.remove(player.getName());
                    player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                    return;
                }
                if (room1.getType() != GameRoom.GameType.END && !room1.close) {
                    PlayerInfo info = room1.getPlayerInfo(player);
                    if (info != null) {
                        info.setPlayer(player);
                        info.setLeave(false);
                        if (room1.getType() == GameRoom.GameType.WAIT) {
                            room1.quitPlayerInfo(info,true);
                            return;
                        } else {
                            if (info.isWatch() || info.getTeamInfo() == null) {
                                room1.joinWatch(info);
                            } else {
                                info.death(null);
                            }
                        }
                    }
                }
            }
            reset(player);
        }else {
            for(GameRoomConfig gameRoomConfig: getRoomConfigs()){
                if(gameRoomConfig.getWorldInfo().getGameWorld() ==  player.level){
                    reset(player);
                }
            }
            if(player.getGamemode() == 3) {
                player.setGamemode(0);
            }
        }

    }


    /**
     * ???????????????????????????
     * */

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event){
        Level level = event.getPosition().getLevel();
        GameRoom room = getGameRoomByLevel(level);
        if(room != null) {
            ArrayList<Block> blocks = new ArrayList<>(event.getBlockList());
            if(room.roomConfig.canBreak.size() > 0){
                for(String block: room.roomConfig.canBreak){
                    int bid = 0;
                    try{
                        bid = Integer.parseInt(block);
                    }catch (Exception ignore){}
                    if(bid > 0) {
                        blocks.add(Block.get(bid));
                    }
                }
            }
            for (Block block : event.getBlockList()) {
                if (!room.worldInfo.getPlaceBlock().contains(block)) {
                    blocks.remove(block);

                }else{
                    room.worldInfo.getPlaceBlock().remove(block);
                }
            }
            event.setBlockList(blocks);
        }
    }



    private void reset(Player player){
        PlayerInfo info = getPlayerInfo(player);
        player.setNameTag(player.getName());
        playerJoin.remove(player.getName());
        player.setHealth(player.getMaxHealth());
        if(info != null){
            player.getInventory().setContents(info.inventory);
            player.getEnderChestInventory().setContents(info.eInventory);
        }
        player.removeAllEffects();
        player.setGamemode(0);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
    }




    /**
     * ????????????????????????????????????
     * */
    @EventHandler
    public void onLevelTransfer(EntityLevelChangeEvent event){
        Entity entity = event.getEntity();
        Level level = event.getTarget();
        GameRoom room = getGameRoomByLevel(level);
        if(entity instanceof Player) {
            PlayerInfo info = getPlayerInfo((Player) entity);
            if(info == null){
                info = new PlayerInfo((Player) entity);
            }
            if (room != null) {
                //??????????????????????????????
                if(info.getPlayerType() == PlayerInfo.PlayerType.WAIT){
                    if(room.equals(info.getGameRoom())){
                        return;
                    }
                }else if(room.equals(info.getGameRoom())){
                    //????????????
                    return;
                }
                if(info.getGameRoom() != null){
                    info.getGameRoom().quitPlayerInfo(info,false);
                }
                switch (room.joinPlayerInfo(info,true)){
                    case CAN_WATCH:
                        room.joinWatch(info);
                        break;
                    case NO_LEVEL:
                    case NO_JOIN:
                        event.setCancelled();
                        TotalManager.sendMessageToObject(language.getLanguage("world-ban-join","&c????????????????????????"),entity);
                        if(Server.getInstance().getDefaultLevel() != null) {
                            info.getPlayer().teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                        }else{
                            info.getPlayer().teleport(info.getPlayer().getLevel().getSafeSpawn());
                        }
                        break;
                    default:break;
                }

            }else{
                if(info.getGameRoom() != null){
                    if(info.isLeave()){
                        return;
                    }

                    if(!info.getGameRoom().getWorldInfo().getConfig().getWaitPosition().getLevel().getFolderName().equalsIgnoreCase(level.getFolderName())) {
                        info.getGameRoom().quitPlayerInfo(info, false);
                    }
                }
            }
        }

    }
    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event){
        for(GameRoomConfig gameRoomConfig: TotalManager.getRoomManager().roomConfig.values()){
            if(gameRoomConfig.getWorldInfo().getGameWorld() != null){
                if(gameRoomConfig.worldInfo.getGameWorld().
                        getFolderName().equalsIgnoreCase(event.getLevel().getFolderName())){
                    event.setCancelled();
                    return;
                }
            }

        }
    }



    /**
     * TODO ??????????????????
     * */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof EntityHuman){
            PlayerInfo playerInfo = getPlayerInfo((EntityHuman) event.getEntity());
            if(playerInfo != null) {

                if (playerInfo.isWatch()) {
                    playerInfo.sendForceMessage(language.getLanguage("player-gamemode-3","&c????????????????????????"));
                    event.setCancelled();
                    return;
                }
                GameRoom room = playerInfo.getGameRoom();
                if (room.getType() == GameRoom.GameType.WAIT) {
                    event.setCancelled();
                    return;
                }

                /////////////
                //?????????
                if (playerInfo.getPlayerType() == PlayerInfo.PlayerType.WAIT) {
                    event.setCancelled();
                    return;
                }

                //TODO ??????????????????
                if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    if (event instanceof EntityDamageByEntityEvent) {
                        Entity damagers = (((EntityDamageByEntityEvent) event).getDamager());
                        if (damagers instanceof Player) {
                            PlayerInfo playerInfo1 = TotalManager.getRoomManager().getPlayerInfo((Player) damagers);
                            if (playerInfo1 != null) {
                                playerInfo1.addSound(Sound.RANDOM_ORB);
                                double h = event.getEntity().getHealth() - event.getFinalDamage();
                                if (h < 0) {
                                    h = 0;
                                }
                                playerInfo1.sendTip(language.getLanguage("player-attack-by-arrow","&e??????: &c???[1]",String.format("%.1f", h)));
                            }

                        }


                    }
                }
                if (event instanceof EntityDamageByEntityEvent) {
                    //TODO ??????TNT????????????
                    Entity entity = ((EntityDamageByEntityEvent) event).getDamager();
                    if (entity instanceof EntityPrimedTNT) {
                        event.setDamage(2);
                    }

                    if (entity instanceof EntityHuman) {
                        PlayerInfo damageInfo = room.getPlayerInfo((EntityHuman) entity);
                        if (damageInfo != null) {
                            if (damageInfo.isWatch()) {
                                event.setCancelled();
                                return;
                            }
                            ///////////////// ????????????PVP///////////////
                            //TODO ????????????PVP
                            TeamInfo t1 = playerInfo.getTeamInfo();
                            TeamInfo t2 = damageInfo.getTeamInfo();
                            if (t1 != null && t2 != null) {
                                if (t1.getTeamConfig().getName().equalsIgnoreCase(t2.getTeamConfig().getName())) {
                                    if(!t1.getTeamConfig().getTeamConfig().isCanPvp()) {
                                        event.setCancelled();
                                        return;
                                    }
                                }
                            }
                            ///////////////// ????????????PVP///////////////
                            playerInfo.setDamageByInfo(damageInfo);
                        } else {
                            event.setCancelled();
                        }
                    }

                }
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    event.setCancelled();
                    playerInfo.death(event);
                }
                if (event.getFinalDamage() + 1 > playerInfo.getPlayer().getHealth()) {
                    event.setCancelled();
                    playerInfo.death(event);
                    for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                        event.setDamage(0, modifier);
                    }
                }
                if(!event.isCancelled()){
                    if(TotalManager.getConfig().getBoolean("display-damage",true)){
                        DamageFloatTextEntity floatTextEntity = new DamageFloatTextEntity(
                                TextFormat.colorize('&',"&c-"+String.format("%.1f",event.getDamage())),
                                event.getEntity().chunk,Entity.getDefaultNBT(
                                event.getEntity().getPosition().add(0,0.8,0)
                        ));
                        floatTextEntity.spawnToAll();

                        floatTextEntity.addMotion(1,0.8,90 / 180f);
                    }
                }
            }
        }
    }




    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        //TODO ???????????? - ???????????????
        Player player = event.getPlayer();
        if(playerJoin.containsKey(player.getName())){
            String roomName = playerJoin.get(player.getName());
            GameRoom room = getRoom(roomName);
            if(room != null){
                if(room.getType() != GameRoom.GameType.START ){
                    PlayerInfo info = room.getPlayerInfo(player);
                    if(info != null){
                        room.quitPlayerInfo(info,true);
                    }

                }else{
                    PlayerInfo info = room.getPlayerInfo(player);
                    if(info != null){
                        if(info.isWatch()){
                            room.quitPlayerInfo(info,true);
                            return;
                        }
                        player.getInventory().clearAll();
                        info.setLeave(true);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Item item = event.getItem();

            if (playerJoin.containsKey(player.getName())) {
                String roomName = playerJoin.get(player.getName());
                GameRoom room = getRoom(roomName);
                if (room != null) {
                    if(item.hasCompoundTag() && item.getNamedTag().getBoolean("quitItem")){
                        event.setCancelled();
                        quitRoomItem(player, roomName, room);
                        return;
                    }
                    if(item.hasCompoundTag() && item.getNamedTag().getBoolean("follow")){
                        followPlayer(room.getPlayerInfo(player),room);
                        event.setCancelled();
                        return;
                    }

                    if(item.hasCompoundTag() && item.getNamedTag().getBoolean("choseTeam")){
                        event.setCancelled();
                        choseteamItem(player, room);

                    }
                    Block block = event.getBlock();

                    if(room.roomConfig.items.size() > 0 && room.roomConfig.roundChest) {
                        if (room.getType() == GameType.START) {
                            ItemConfig config = room.getRandomItemConfig(block);
                            if (config != null) {
                                BlockEntity entityChest = block.level.getBlockEntity(block);
                                if (entityChest instanceof InventoryHolder && entityChest instanceof BlockEntityNameable) {
                                    PlayerInfo playerInfo = room.getPlayerInfo(player);
                                    if(playerInfo != null){
                                        if(playerInfo.getTeamInfo() != null){
                                            if(!playerInfo.getTeamInfo().getTeamConfig().getTeamConfig().isOpenChest()){
                                                event.setCancelled();
                                                return;
                                            }
                                        }else{
                                            event.setCancelled();
                                            return;
                                        }
                                        LinkedHashMap<Integer, Item> items = room.getRandomItem(((InventoryHolder) entityChest)
                                                .getInventory().getSize(), block);
                                        if (items.size() > 0) {
                                            ((InventoryHolder) entityChest).getInventory().setContents(items);
                                        }
                                    }


                                }

                            }
                        }
                    }
                }
            }
        }

    }

    private void choseteamItem(Player player, GameRoom room) {
        if(!TeamChoseItem.clickAgain.contains(player)){
            TeamChoseItem.clickAgain.add(player);
            player.sendTip(language.getLanguage("chose-click-again","??????????????????"));
            return;
        }
        FormWindowSimple simple = new FormWindowSimple(language.getLanguage("player-chose-team","???????????????"),"");
        for(TeamInfo teamInfoConfig: room.getTeamInfos()){
            Item wool = teamInfoConfig.getTeamConfig().getTeamConfig().getBlockWoolColor();
            simple.addButton(new ElementButton(TextFormat.colorize('&', teamInfoConfig +" &r"+teamInfoConfig.getTeamPlayers().size()+" / "+(room.getRoomConfig().getMaxPlayerSize() / room.getTeamInfos().size())),
                    new ElementButtonImageData("path",
                            ItemIDSunName.getIDByPath(wool.getId(),wool.getDamage()))));
        }
        player.showFormWindow(simple,102);
        TeamChoseItem.clickAgain.remove(player);
    }

    private void followPlayer(PlayerInfo info,GameRoom room){
        info.sendMessage(language.getLanguage("player-chose-teleport-player","????????????????????????"));
        if (room == null){
            return;
        }
        disPlayUI(info, room);

    }

    private void disPlayProtect(PlayerInfo info,GameRoom room){
        List<BaseIButton> list = new ArrayList<>();
        //????????????
        for(PlayerInfo i: room.getLivePlayers()){
            list.add(new BaseIButton(new PlayerItem(i).getGUIButton(info)) {
                @Override
                public void onClick(Player player) {
                    player.teleport(i.getPlayer().getLocation());
                }
            });
        }
        DisPlayWindowsFrom.disPlayerCustomMenu((Player) info.getPlayer(), language.getLanguage("player-from-teleport-player-title","????????????"), list);

    }


    private void disPlayUI(PlayerInfo info, GameRoom room){
        //WIN10 ?????? ??????????????????
//        DisPlayerPanel playerPanel = new DisPlayerPanel();
//        playerPanel.displayPlayer(info,DisPlayerPanel.displayPlayers(room),"????????????");

        disPlayProtect(info, room);
    }

    private boolean quitRoomItem(Player player, String roomName, GameRoom room) {
        if(!RoomQuitItem.clickAgain.contains(player)){
            RoomQuitItem.clickAgain.add(player);
            player.sendTip(language.getLanguage("chose-click-again","??????????????????"));
            return true;
        }
        RoomQuitItem.clickAgain.remove(player);
        if(room.quitPlayerInfo(room.getPlayerInfo(player),true)){
            player.sendMessage(language.getLanguage("player-quit-room-success","????????????????????? [1]",roomName));
        }
        return false;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(playerJoin.containsKey(player.getName())) {
            String roomName = playerJoin.get(player.getName());
            GameRoom room = getRoom(roomName);
            if (room != null) {
                if(room.getType() == GameType.WAIT){
                    event.setCancelled();
                    return;
                }
                Item item = event.getItem();
                if (item.hasCompoundTag() && item.getNamedTag().contains(TotalManager.GAME_NAME)) {
                    event.setCancelled();
                }
            }
        }
    }


    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        if(playerJoin.containsKey(player.getName())){
            String roomName = playerJoin.get(player.getName());
            GameRoom room = getRoom(roomName);
            if(room != null){
                Item item = event.getItem();
                if(item.hasCompoundTag() && "quitItem".equalsIgnoreCase(item.getNamedTag().getString(TotalManager.GAME_NAME))){
                    player.getInventory().setHeldItemSlot(0);
                    if (quitRoomItem(player, roomName, room)) {
                        return;
                    }
                }
                if(item.hasCompoundTag() && "choseTeam".equalsIgnoreCase(item.getNamedTag().getString(TotalManager.GAME_NAME))){
                    player.getInventory().setHeldItemSlot(0);
                    choseteamItem(player, room);


                }
                if(item.hasCompoundTag() && "follow".equalsIgnoreCase(item.getNamedTag().getString(TotalManager.GAME_NAME))){
                    followPlayer(room.getPlayerInfo(player),room);
                    player.getInventory().setHeldItemSlot(0);
                }
            }
        }
    }



    @EventHandler
    public void onFrom(PlayerFormRespondedEvent event){
        if(event.wasClosed()){
            return;
        }
        Player player = event.getPlayer();
        if(DisPlayWindowsFrom.FROM.containsKey(player.getName())){
            GameFrom simple = DisPlayWindowsFrom.FROM.get(player.getName());
            if (onGameFrom(event, player, simple)) {
                return;
            }

        }
        int fromId = 102;
        if(event.getFormID() == fromId && event.getResponse() instanceof FormResponseSimple){
            PlayerInfo info = TotalManager.getRoomManager().getPlayerInfo(player);
            if(info != null){
                if(info.getGameRoom() == null || info.getGameRoom().getType() == GameType.START){
                    return;
                }
                TeamInfo teamInfo = info.getGameRoom().getTeamInfos().get(((FormResponseSimple) event.getResponse())
                        .getClickedButtonId());
                if(!teamInfo.join(info)){
                    info.sendMessage(language.getLanguage("player-joined-team","&c?????????????????? [1]",teamInfo.toString()));
                }else{
                    info.sendMessage(language.getLanguage("player-join-team-success","&a??????&r[1] &a??????",teamInfo.toString()));
                    player.getInventory().setItem(0,teamInfo.getTeamConfig().getTeamConfig().getBlockWoolColor());
                    for (Map.Entry<Integer, Item> entry : info.armor.entrySet()) {
                        Item item;
                        if(entry.getValue() instanceof ItemColorArmor){
                            ItemColorArmor colorArmor = (ItemColorArmor) entry.getValue();
                            colorArmor.setColor(teamInfo.getTeamConfig().getRgb());
                            item = colorArmor;
                        }else{
                            item = entry.getValue();
                        }
                        player.getInventory().setArmorItem(entry.getKey(), item);
                    }
                }
            }

        }

    }

    private boolean onGameFrom(PlayerFormRespondedEvent event, Player player, GameFrom simple) {
        if(simple.getId() == event.getFormID()) {
            if (event.getResponse() instanceof FormResponseSimple) {
                BaseIButton button = simple.getBaseIButtons().get(((FormResponseSimple) event.getResponse())
                        .getClickedButtonId());
                button.onClick(player);
            }
            return true;

        }else{
            DisPlayWindowsFrom.FROM.remove(player.getName());
        }
        return false;
    }

    @EventHandler
    public void onItemChange(InventoryTransactionEvent event) {
        InventoryTransaction transaction = event.getTransaction();
        for (InventoryAction action : transaction.getActions()) {
            for (Inventory inventory : transaction.getInventories()) {
                if (inventory instanceof ChestInventoryPanel) {
                    Player player = ((ChestInventoryPanel) inventory).getPlayer();
                    event.setCancelled();
                    Item i = action.getSourceItem();
                    if(i.hasCompoundTag() && i.getNamedTag().contains("index")){
                        int index = i.getNamedTag().getInt("index");
                        BasePlayPanelItemInstance item = ((ChestInventoryPanel) inventory).getPanel().getOrDefault(index,null);

                        if(item != null){
                            ((ChestInventoryPanel) inventory).clickSolt = index;
                            item.onClick((ChestInventoryPanel) inventory,player);
                            ((ChestInventoryPanel) inventory).update();
                        }
                    }

                }
                if(inventory instanceof PlayerInventory){
                    EntityHuman player =((PlayerInventory) inventory).getHolder();
                    PlayerInfo playerInfo = getPlayerInfo(player);
                    if(playerInfo != null){
                        GameRoom gameRoom = playerInfo.getGameRoom();
                        if(gameRoom != null){
                            if(gameRoom.getType() == GameType.WAIT){
                                event.setCancelled();
                            }
                        }
                    }
                }
            }
        }
    }



    @EventHandler
    public void onExecuteCommand(PlayerCommandPreprocessEvent event){
        PlayerInfo info = getPlayerInfo(event.getPlayer());
        if(info != null){
            GameRoom room = info.getGameRoom();
            if(room != null) {
                for(String cmd: room.getRoomConfig().banCommand){
                    if(event.getMessage().contains(cmd)){
                        event.setCancelled();
                    }
                }
            }
        }

    }




    @EventHandler
    public void onCraft(CraftItemEvent event){
        Player player = event.getPlayer();
        GameRoom room = getGameRoomByLevel(player.getLevel());
        if(room != null) {
            PlayerInfo info = room.getPlayerInfo(player);
            if (info != null) {
                event.setCancelled();
            }
        }
    }

    /**
     * ??????????????????????????????

     * */
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event){
        Level level = event.getBlock().level;

        Block block = event.getBlock();
        Item item = event.getItem();
        if(item.hasCompoundTag() && (item.getNamedTag().contains(TotalManager.GAME_NAME)
        )){
            event.setCancelled();
            return;
        }
        GameRoom room = getGameRoomByLevel(level);
        if(room != null){
            PlayerInfo info = room.getPlayerInfo(event.getPlayer());
            if(info != null) {
                if (info.isWatch()) {
                    info.sendMessage(language.getLanguage("event-place-block-cancel-watch","&c?????????????????????????????????"));
                    event.setCancelled();

                }else{
                    room.worldInfo.onChangeBlock(block,true);
                }

            }
            //TODO ??????TNT
            if (block instanceof BlockTNT) {
                try{
                    event.setCancelled();
                    ((BlockTNT) block).prime(60);
                    Item i2 = item.clone();
                    i2.setCount(1);
                    event.getPlayer().getInventory().removeItem(i2);
                }catch (Exception e){
                    event.setCancelled();

                }

            }
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent event){
        //TODO ??????TNT?????? ???????????????????????????
        Entity entity = event.getEntity();
        GameRoom room = getGameRoomByLevel(entity.level);
        if(room != null) {
            if (entity instanceof EntityPrimedTNT && !(entity instanceof EntityTnt)) {
                //??????????????????????????????????????????
                EntityTnt entityTnt = new EntityTnt(entity.chunk,entity.namedTag);
                entityTnt.setTick(60);
                entityTnt.spawnToAll();
                entity.close();

            }
        }
    }

    /**
     * ??????????????????????????????

     * */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Level level = event.getBlock().level;

        Item item = event.getItem();
        if(item.hasCompoundTag() && (item.getNamedTag().contains(TotalManager.GAME_NAME)
        )){
            event.setCancelled();
            return;
        }
        Block block = event.getBlock();
        GameRoom room = getGameRoomByLevel(level);
        if(room != null){
            PlayerInfo info = room.getPlayerInfo(event.getPlayer());
            if(info != null) {
                if(info.isWatch()) {
                    info.sendMessage(language.getLanguage("event-break-block-cancel","&c????????????????????????"));
                    event.setCancelled();
                }
                if(room.roomConfig.banBreak.size() > 0){
                    if(room.roomConfig.banBreak.contains(block.getId()+"")){
                        if(!room.roomConfig.canBreak.contains(block.getId()+"")){
                            event.setCancelled();
                        }else{
                            room.addSound(Sound.BLOCK_END_PORTAL_FRAME_FILL);
                        }
                    }
                    room.worldInfo.onChangeBlock(block, false);

                }else {

                    if (room.worldInfo.getPlaceBlock().contains(block) || room.roomConfig.canBreak.contains(block.getId()+"")) {
                        room.worldInfo.onChangeBlock(block, false);
                    } else {
                        if(!room.roomConfig.canBreak.contains(block.getId()+"")){
                            info.sendMessage(language.getLanguage("event-break-block-cancel","&c????????????????????????"));
                            event.setCancelled();
                        }else{
                            room.addSound(Sound.BLOCK_END_PORTAL_FRAME_FILL);
                        }
                    }
                }
            }
        }
    }
    /**
     * ??????????????????????????????

     * */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event){
        PlayerInfo info = getPlayerInfo(event.getPlayer());
        if(info != null){
            GameRoom room = info.getGameRoom();
            if(room != null){
                if(info.isWatch()){
                    room.sendMessageOnWatch(info+" &r>> "+event.getMessage());
                }else{
                    String msg = event.getMessage();
                    if(msg.startsWith("@") || msg.startsWith("!")){
//
                        info.getGameRoom().sendFaceMessage( language.getLanguage("player-speak-in-room-message-all","&l&7(????????????)&r [1]&r >> [2]",
                            info.toString(),msg.substring(1)));
                    }else{
                        TeamInfo teamInfo = info.getTeamInfo();
                        if(teamInfo != null){
                            if(info.isDeath()){
                                room.sendMessageOnDeath(language.getLanguage("player-speak-in-room-message-death",
                                        "[1]&7(??????) &r>> [2]",
                                        info.toString(),msg));
                            }else {
                                teamInfo.sendMessage(language.getLanguage("player-speak-in-room-message-team",  "[1][??????]&7[2] &f>>&r [3]",
                                        teamInfo.getTeamConfig().getNameColor(),
                                        info.getPlayer().getName(),
                                        msg));
                            }
                        }else{
                            room.sendMessage(language.getLanguage("player-speak-in-room-message","[1] &f>>&r [2]",
                                    info.toString(),
                                    msg));
                        }
                    }
                }
                event.setCancelled();
            }
        }
    }



}
