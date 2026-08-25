package dev.dangeroni.renewablelava.rule;

import dev.dangeroni.renewablelava.config.RenewableLavaConfig;
import dev.dangeroni.renewablelava.state.RenewableLavaWorldState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public final class RenewableLavaRules {
	private RenewableLavaRules() {
	}

	public static boolean isLavaSourceConversionEnabled(ServerLevel level) {
		RenewableLavaConfig config = RenewableLavaConfig.get();
		Identifier dimensionId = level.dimension().identifier();
		boolean worldEnabled = RenewableLavaWorldState.get(level.getServer()).isEnabled();
		boolean dimensionWhitelisted = config.isDimensionWhitelisted(dimensionId);
		return evaluate(config.enabled(), worldEnabled, dimensionWhitelisted);
	}

	public static int getRequiredSourceNeighbours() {
		return RenewableLavaConfig.get().requiredSourceNeighbours();
	}

	static boolean evaluate(boolean configEnabled, boolean worldEnabled, boolean dimensionWhitelisted) {
		return configEnabled && worldEnabled && dimensionWhitelisted;
	}
}
