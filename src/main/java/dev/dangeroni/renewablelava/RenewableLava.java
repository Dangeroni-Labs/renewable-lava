package dev.dangeroni.renewablelava;

import dev.dangeroni.renewablelava.command.RenewableLavaCommand;
import dev.dangeroni.renewablelava.config.RenewableLavaConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenewableLava implements ModInitializer {
	public static final String MOD_ID = "renewable_lava";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RenewableLavaConfig.load();
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> RenewableLavaCommand.register(dispatcher));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
