package org.sobadfish.zombiepvp.tools;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.DyeColor;
import org.sobadfish.zombiepvp.manager.TotalManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.SplittableRandom;


public class Utils {

    private static final SplittableRandom RANDOM = new SplittableRandom(System.currentTimeMillis());

    public static int rand(int min, int max) {
        return min == max ? max : RANDOM.nextInt(max + 1 - min) + min;
    }

    public static double rand(double min, double max) {
        return min == max ? max : min + Math.random() * (max - min);
    }

    public static float rand(float min, float max) {
        return min == max ? max : min + (float) Math.random() * (max - min);
    }

    public static boolean rand() {
        return RANDOM.nextBoolean();
    }

    public static double calLinearFunction(Vector3 pos1, Vector3 pos2, double element, int type) {
        if (pos1.getFloorY() != pos2.getFloorY()) {
            return 1.7976931348623157E308D;
        } else if (pos1.getX() == pos2.getX()) {
            return type == 1 ? pos1.getX() : 1.7976931348623157E308D;
        } else if (pos1.getZ() == pos2.getZ()) {
            return type == 0 ? pos1.getZ() : 1.7976931348623157E308D;
        } else {
            return type == 0 ? (element - pos1.getX()) * (pos1.getZ() - pos2.getZ()) / (pos1.getX() - pos2.getX()) + pos1.getZ() : (element - pos1.getZ()) * (pos1.getX() - pos2.getX()) / (pos1.getZ() - pos2.getZ()) + pos1.getX();
        }
    }

    /**
     * ??????????????????????????? x??????????????????
     * @param player ???????????????
     * @param size ??????
     * @return ??????????????????
     * */
    public static ArrayList<Player> getAroundOfPlayers(Position player, int size) {
        ArrayList<Player> players = new ArrayList<>();
        for (Entity entity : getAroundPlayers(player, size, false)) {
            players.add((Player) entity);
        }
        return players;
    }

    /**
     * ??????????????????????????? x??????????????????
     * @param player ???????????????
     * @param size ??????
     * @param isEntity ??????????????????
     * @return ??????????????????
     * */
    public static LinkedList<Entity> getAroundPlayers(Position player, int size, boolean isEntity) {
        LinkedList<Entity> explodePlayer = new LinkedList<>();
        for (Entity player1 : player.level.getEntities()) {

            if (player1.x < player.x + size && player1.x > player.x - size && player1.z < player.z + size && player1.z > player.z - size && player1.y < player.y + size && player1.y > player.y - size) {
                if (!isEntity && player instanceof Player && ((Player) player).getGamemode() != 3) {
                    explodePlayer.add(player1);
                } else if (isEntity && !(player1 instanceof BaseEntity)) {
                    explodePlayer.add(player1);
                }

            }
        }
        return explodePlayer;
    }


    public static void toDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    toDelete(file1);
                } else {
                    file1.delete();
                }
            }
        }

        file.delete();

    }

    /**
     * ?????????
     */
    public static void spawnFirework(Position position) {

        Level level = position.getLevel();
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        Random random = new Random();
        CompoundTag ex = new CompoundTag();
        ex.putByteArray("FireworkColor", new byte[]{
                (byte) DyeColor.values()[random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
        });
        ex.putByteArray("FireworkFade", new byte[0]);
        ex.putBoolean("FireworkFlicker", random.nextBoolean());
        ex.putBoolean("FireworkTrail", random.nextBoolean());
        ex.putByte("FireworkType", ItemFirework.FireworkExplosion.ExplosionType.values()
                [random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks", (new CompoundTag("Fireworks")).putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight", 1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag();
        nbt.putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("", position.x + 0.5D))
                .add(new DoubleTag("", position.y + 0.5D))
                .add(new DoubleTag("", position.z + 0.5D))
        );
        nbt.putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("", 0.0D))
                .add(new DoubleTag("", 0.0D))
                .add(new DoubleTag("", 0.0D))
        );
        nbt.putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("", 0.0F))
                .add(new FloatTag("", 0.0F))

        );
        nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item));
        EntityFirework entity = new EntityFirework(level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        entity.spawnToAll();
    }



    /**
     * ??????????????????????????????
     * */
    public static String writeLine(int size,String line){
        StringBuilder s = new StringBuilder();
        for(int i = 0;i< size;i++){
            s.append(line);
        }
        return s.toString();
    }






    /**
     * ????????????
     * */
    public static boolean copyFiles(File old,File target){

        int load = 1;
        File[] files = old.listFiles();
        if(files != null){
            for (File value : files) {
                TotalManager.sendMessageToConsole("Coping world ... "+((load / (float)files.length) * 100) +"%");
                load++;
                if (value.isFile()) {
                    // ????????????
                    try {
                        File file1 = new File(target +File.separator + value.getName());
                        if(!file1.exists()){
                            try{
                                file1.createNewFile();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        copyFile(value, file1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (value.isDirectory()) {
                    // ????????????
                    String sourceDir = old + File.separator + value.getName();
                    String targetDir = target+ File.separator + value.getName();
                    try {
                        copyDirectiory(sourceDir, targetDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return true;
    }

    private static void copyFile(File source,File target)
            throws IOException{

        RandomAccessFile sourceFile = new RandomAccessFile(source, "r");
        FileChannel sourceChannel = sourceFile.getChannel();

        if (!target.isFile()) {
            if (!target.createNewFile()) {
                sourceChannel.close();
                sourceFile.close();
                return;
            }
        }
        RandomAccessFile destFile = new RandomAccessFile(target, "rw");
        FileChannel destChannel = destFile.getChannel();
        long leftSize = sourceChannel.size();
        long position = 0;
        while (leftSize > 0) {
            long writeSize = sourceChannel.transferTo(position, leftSize, destChannel);
            position += writeSize;
            leftSize -= writeSize;
        }
        sourceChannel.close();
        sourceFile.close();
        destChannel.close();
        destFile.close();
    }

    /**???????????????   */
    private static void copyDirectiory(String sourceDir, String targetDir)
            throws IOException {
        // ??????????????????
        File file = new File(targetDir);
        if(!file.exists()) {
            if (!file.mkdirs()) {
                Server.getInstance().getLogger().error("create" + targetDir + "error");
            }
        }
        // ?????????????????????????????????????????????
        File[] files = (new File(sourceDir)).listFiles();
        if(files != null){
            for (File value : files) {
                if (value.isFile()) {
                    // ?????????
                    // ????????????
                    File targetFile = new
                            File(new File(targetDir).getAbsolutePath()
                            + File.separator + value.getName());
                    copyFile(value, targetFile);

                }
                if (value.isDirectory()) {
                    // ???????????????????????????
                    String dir1 = sourceDir + File.separator + value.getName();
                    // ??????????????????????????????
                    String dir2 = targetDir + File.separator + value.getName();
                    copyDirectiory(dir1, dir2);
                }
            }
        }

    }


}
