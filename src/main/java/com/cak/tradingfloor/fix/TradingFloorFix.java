package com.cak.tradingfloor.fix;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TradingFloorFix.MOD_ID)
public class TradingFloorFix {

    public static final String MOD_ID = "tradingfloorfix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TradingFloorFix() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем шину Forge-ивентов
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[TradingFloorFix] Mod initialized. Waiting for trades...");
    }
}
