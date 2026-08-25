package dev.dangeroni.renewablelava;

import com.mojang.logging.LogUtils;
import dev.dangeroni.renewablelava.command.RenewableLavaCommand;
import dev.dangeroni.renewablelava.config.RenewableLavaConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(RenewableLava.MOD_ID)
public final class RenewableLava {
    public static final String MOD_ID = "renewable_lava";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RenewableLava(IEventBus modEventBus) {
        RenewableLavaConfig.load();
        NeoForge.EVENT_BUS.addListener(RenewableLavaCommand::register);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
