package com.cak.tradingfloor.fix;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on the Forge EVENT_BUS every time trade count updates for a player.
 * totalTrades — общее кол-во трейдов игрока на данный момент.
 * depotPos — позиция депо (null если игрок был офлайн и ивент кинут при логине).
 */
public class TradingTradeEvent extends Event {

    private final ServerPlayer player;
    private final int totalTrades;
    @Nullable
    private final BlockPos depotPos;

    public TradingTradeEvent(ServerPlayer player, int totalTrades, @Nullable BlockPos depotPos) {
        this.player = player;
        this.totalTrades = totalTrades;
        this.depotPos = depotPos;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getTotalTrades() { return totalTrades; }
    @Nullable
    public BlockPos getDepotPos() { return depotPos; }
}
