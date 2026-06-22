package com.cak.tradingfloor.fix.mixin;

import com.cak.tradingfloor.fix.TradeEventBridge;
import com.cak.trading_floor.content.trading_depot.behavior.CommonTradingDepotBehaviorAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(targets = "com.cak.trading_floor.forge.content.depot.TradingDepotBlockEntity", remap = false)
public abstract class TradingDepotBlockEntityMixin {

    private BlockPos getOwnPos() {
        return ((BlockEntity) (Object) this).getBlockPos();
    }

    @Inject(
        method = "tryTradeWith",
        at = @At("RETURN"),
        remap = false
    )
    private void tradingfloorfix$onTradeCompleted(
        Villager villager,
        List<CommonTradingDepotBehaviorAccess> allDepots,
        CallbackInfo ci
    ) {
        try {
            if (villager == null) return;

            Level level = villager.level();
            if (level.isClientSide()) return;

            BlockPos depotPos = getOwnPos();
            MinecraftServer server = level.getServer();
            if (server == null) return;

            // Читаем UUID владельца из NBT блока
            BlockEntity be = level.getBlockEntity(depotPos);
            if (be == null) return;

            CompoundTag nbt = be.saveWithoutMetadata();
            if (!nbt.contains("Owner")) {
                com.cak.tradingfloor.fix.TradingFloorFix.LOGGER.warn(
                    "[TradingFloorFix] No owner for depot at {} — place the block as a player first", depotPos
                );
                return;
            }

            UUID ownerUUID = nbt.getUUID("Owner");

            // Сохраняем счётчик и кидаем ивент (работает и для офлайн-игроков)
            TradeEventBridge.onTradeCompleted(server, ownerUUID, depotPos);

        } catch (Exception e) {
            com.cak.tradingfloor.fix.TradingFloorFix.LOGGER.error(
                "[TradingFloorFix] Error in mixin", e
            );
        }
    }
}
