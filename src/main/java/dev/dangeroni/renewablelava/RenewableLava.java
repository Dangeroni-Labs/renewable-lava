package dev.dangeroni.renewablelava;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class RenewableLava implements ModInitializer {
	public static final String MOD_ID = "renewable_lava";

	@Override
	public void onInitialize() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
