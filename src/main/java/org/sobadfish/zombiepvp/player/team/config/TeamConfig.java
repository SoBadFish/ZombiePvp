package org.sobadfish.zombiepvp.player.team.config;


import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.BlockColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 团队的基本数据
 * @author SoBadFish
 * 2022/1/2
 */

public class TeamConfig {

    /**团队的名称*/
    private final String name;

    /**
     *
     * 是否允许队伍内伤害
     * */
    private boolean canPvp;

    /**
     * 盔甲
     * */
    private LinkedHashMap<Integer,Item> inventoryArmor = new LinkedHashMap<>();

    /**
     * 队伍初始物品
     * */
    private LinkedHashMap<Integer,Item> inventoryItem = new LinkedHashMap<>();


    /**团队的颜色符号*/
    private final String nameColor;

    /**
     * 团队的代表方块物品(不给玩家也行，这个可以用作GUI)
     * */
    private final Item blockWoolColor;

    private boolean canInfection;

    public int maxPlayer = -1;

    public int victoryWeight = 0;

    public int defaultHealth = 20;

    public int teamSpawnCount = 0;

    public boolean openChest = true;

    public int deathPoint = 1;

    public String spawnMessage = "";

    public List<Effect> teamEffect = new ArrayList<>();


    /**
     * 团队皮革衣服的颜色
     * */
    private final BlockColor rgb;

    private TeamConfig(String name, String nameColor, Item blockWoolColor, BlockColor rgb){
        this.name = name;
        this.nameColor = nameColor;
        this.blockWoolColor = blockWoolColor;
        this.rgb = rgb;
    }

    public void setCanPvp(boolean canPvp) {
        this.canPvp = canPvp;
    }

    public boolean isCanPvp() {
        return canPvp;
    }

    public LinkedHashMap<Integer, Item> getInventoryArmor() {
        return inventoryArmor;
    }

    public String getName() {
        return name;
    }

    public void setVictoryWeight(int victoryWeight) {
        this.victoryWeight = victoryWeight;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public void setTeamSpawnCount(int teamSpawnCount) {
        this.teamSpawnCount = teamSpawnCount;
    }

    public int getTeamSpawnCount() {
        return teamSpawnCount;
    }

    public int getVictoryWeight() {
        return victoryWeight;
    }

    public LinkedHashMap<Integer, Item> getInventoryItem() {
        return inventoryItem;
    }

    public Item getBlockWoolColor() {
        return blockWoolColor;
    }

    public BlockColor getRgb() {
        return rgb;
    }


    public void setTeamEffect(List<Effect> teamEffect) {
        this.teamEffect = teamEffect;
    }

    public List<Effect> getTeamEffect() {
        return teamEffect;
    }

    public void setInventoryArmor(LinkedHashMap<Integer, Item> inventoryArmor) {
        this.inventoryArmor = inventoryArmor;
    }

    public void setInventoryItem(LinkedHashMap<Integer, Item> inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    public String getNameColor() {
        return nameColor;
    }

    public void setOpenChest(boolean openChest) {
        this.openChest = openChest;
    }

    public boolean isOpenChest() {
        return openChest;
    }

    public void setDeathPoint(int deathPoint) {
        this.deathPoint = deathPoint;
    }

    public int getDeathPoint() {
        return deathPoint;
    }

    public void setDefaultHealth(int defaultHealth) {
        this.defaultHealth = defaultHealth;
    }

    public int getDefaultHealth() {
        return defaultHealth;
    }

    public void setSpawnMessage(String spawnMessage) {
        this.spawnMessage = spawnMessage;
    }

    public String getSpawnMessage() {
        return spawnMessage;
    }

    public void setCanInfection(boolean canInfection) {
        this.canInfection = canInfection;
    }

    public boolean isCanInfection() {
        return canInfection;
    }

    /**
     * 解析 team.yml 配置文件
     * */
    public static TeamConfig getInstance(Map<?,?> map){
        String name = map.get("name").toString();
        String nameColor = map.get("nameColor").toString();
        Map<?,?> m = (Map<?,?>) map.get("rgb");
        int r = Integer.parseInt(m.get("r").toString());
        int g = Integer.parseInt(m.get("g").toString());
        int b = Integer.parseInt(m.get("b").toString());
        TeamConfig teamConfig = new TeamConfig(name,nameColor, Item.fromString(map.get("blockWoolColor")
                .toString()),new BlockColor(r,g,b));
        if(map.containsKey("canPvp")){
            teamConfig.setCanPvp(Boolean.parseBoolean(map.get("canPvp").toString()));
        }
        if(map.containsKey("openChest")){
            teamConfig.setOpenChest(Boolean.parseBoolean(map.get("openChest").toString()));
        }
        if(map.containsKey("canInfection")){
            teamConfig.setCanInfection(Boolean.parseBoolean(map.get("canInfection").toString()));
        }
        if(map.containsKey("victoryWeight")){
            teamConfig.setVictoryWeight(Integer.parseInt(map.get("victoryWeight").toString()));
        }
        if(map.containsKey("deathPoint")){
            teamConfig.setDeathPoint(Integer.parseInt(map.get("deathPoint").toString()));
        }
        if(map.containsKey("defaultHealth")){
            teamConfig.setDefaultHealth(Integer.parseInt(map.get("defaultHealth").toString()));
        }
        if(map.containsKey("maxPlayer")){
            teamConfig.setMaxPlayer(Integer.parseInt(map.get("maxPlayer").toString()));
        }
        if(map.containsKey("spawnMessage")){
            teamConfig.setSpawnMessage(map.get("spawnMessage").toString());
        }
        if(map.containsKey("teamSpawnCount")){
            teamConfig.setTeamSpawnCount(Integer.parseInt(map.get("teamSpawnCount").toString()));
        }
        if(map.containsKey("teamEffect")){
            List<Effect> effect = new ArrayList<>();
            for(Object o: (List<?>)map.get("teamEffect")){
                String[] e = o.toString().split(":");
                Effect effect1 = Effect.getEffect(Integer.parseInt(e[0]));
                if(e.length > 1){
                    effect1.setAmplifier(Integer.parseInt(e[1]));
                }
                effect.add(effect1);
            }
            teamConfig.setTeamEffect(effect);
        }

        if(map.containsKey("inventory")){
            Map<?,?> inventoryMap = (Map<?, ?>) map.get("inventory");
            if(inventoryMap.containsKey("armor")){
                teamConfig.setInventoryArmor(decodeItemList((List<?>) inventoryMap.get("armor")));
            }
            if(inventoryMap.containsKey("inventory")){
                teamConfig.setInventoryItem(decodeItemList((List<?>) inventoryMap.get("inventory")));
            }
        }
        return teamConfig;
    }





    //解析物品对象
    private static LinkedHashMap<Integer,Item> decodeItemList(List<?> list){
        LinkedHashMap<Integer,Item> items = new LinkedHashMap<>();
        int i = 0;
        for(Object o: list){
            String[] s = o.toString().split(":");
            int itemId = 0;
            try{
                itemId = Integer.parseInt(s[0]);
            }catch (Exception ignored){}
            int itemDamage = 0;
            if(s.length > 1){
                try {
                    itemDamage = Integer.parseInt(s[1]);
                }catch (Exception ignored){}
            }
            int itemCount = 0;
            if(s.length > 2){
                try {
                    itemCount = Integer.parseInt(s[2]);
                }catch (Exception ignored){}
            }

            Item item =  Item.get(itemId,itemDamage);
            item.setCount(itemCount);

            if(item.getId() > 0) {
                items.put(i,item);
            }
            i++;
        }
        return items;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof TeamConfig){
            return ((TeamConfig) obj).getName().equalsIgnoreCase(getName());
        }
        return false;
    }

}
