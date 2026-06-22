package com.cak.tradingfloor.fix;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TradeEventBridge {

    // Папка для хранения счётчиков: world/tradingfloorfix/<uuid>.dat
    private static File getSaveDir(MinecraftServer server) {
        File dir = new File(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "tradingfloorfix");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static File getPlayerFile(MinecraftServer server, UUID uuid) {
        return new File(getSaveDir(server), uuid + ".dat");
    }

    // Читаем счётчик с диска
    public static int getTradeCount(MinecraftServer server, UUID uuid) {
        File file = getPlayerFile(server, uuid);
        if (!file.exists()) return 0;
        try {
            CompoundTag tag = NbtIo.read(file);
            return tag != null ? tag.getInt("trades") : 0;
        } catch (IOException e) {
            TradingFloorFix.LOGGER.error("[TradingFloorFix] Failed to read trade count for {}", uuid, e);
            return 0;
        }
    }

    // Записываем счётчик на диск
    public static void setTradeCount(MinecraftServer server, UUID uuid, int count) {
        File file = getPlayerFile(server, uuid);
        try {
            CompoundTag tag = new CompoundTag();
            tag.putInt("trades", count);
            NbtIo.write(tag, file);
        } catch (IOException e) {
            TradingFloorFix.LOGGER.error("[TradingFloorFix] Failed to save trade count for {}", uuid, e);
        }
    }

    // Вызывается из Mixin — игрок может быть онлайн или офлайн
    public static void onTradeCompleted(MinecraftServer server, UUID ownerUUID, BlockPos depotPos) {
        int current = getTradeCount(server, ownerUUID) + 1;
        setTradeCount(server, ownerUUID, current);

        TradingFloorFix.LOGGER.info("[TradingFloorFix] Trade for {} ({}), total: {}", ownerUUID, depotPos, current);

        // Кидаем ивент только если игрок онлайн
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(ownerUUID);
        if (onlinePlayer != null) {
            MinecraftForge.EVENT_BUS.post(new TradingTradeEvent(onlinePlayer, current, depotPos));
        }
        // Если офлайн — ивент кинем когда зайдёт (см. PlayerLoginHandler)
    }

    // Вызывается при входе игрока — проверяем накопленные трейды
    public static void onPlayerLogin(ServerPlayer player) {
        MinecraftServer server = player.server;
        UUID uuid = player.getUUID();
        int count = getTradeCount(server, uuid);

        if (count > 0) {
            TradingFloorFix.LOGGER.info("[TradingFloorFix] Player {} logged in with {} pending trades", player.getName().getString(), count);
            MinecraftForge.EVENT_BUS.post(new TradingTradeEvent(player, count, null));
        }
    }
}
