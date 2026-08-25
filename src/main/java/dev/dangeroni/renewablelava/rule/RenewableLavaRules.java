package dev.dangeroni.renewablelava.rule;

import dev.dangeroni.renewablelava.config.RenewableLavaConfig;
import dev.dangeroni.renewablelava.state.RenewableLavaWorldState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

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

	public static boolean shouldConvertToSource(FlowingFluid fluid, ServerLevel level, BlockPos pos) {
		return isLavaSourceConversionEnabled(level)
			&& hasRequiredSourceNeighbours(countHorizontalSourceNeighbours(level, pos, fluid))
			&& hasSourceSupport(level, pos, fluid);
	}

	public static boolean hasRequiredSourceNeighbours(int sourceNeighbours) {
		return hasRequiredSourceNeighbours(sourceNeighbours, getRequiredSourceNeighbours());
	}

	static boolean hasRequiredSourceNeighbours(int sourceNeighbours, int requiredSourceNeighbours) {
		return sourceNeighbours >= requiredSourceNeighbours;
	}

	static int countHorizontalSourceNeighbours(LevelReader level, BlockPos pos, FlowingFluid fluid) {
		int count = 0;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			FluidState neighbour = level.getFluidState(pos.relative(direction));
			if (isMatchingSource(neighbour, fluid)) {
				count++;
			}
		}

		return count;
	}

	private static boolean hasSourceSupport(LevelReader level, BlockPos pos, FlowingFluid fluid) {
		BlockPos belowPos = pos.below();
		FluidState belowFluid = level.getFluidState(belowPos);
		return level.getBlockState(belowPos).isSolid() || isMatchingSource(belowFluid, fluid);
	}

	private static boolean isMatchingSource(FluidState state, FlowingFluid fluid) {
		return state.getType().isSame(fluid) && state.isSource();
	}

	static boolean evaluate(boolean configEnabled, boolean worldEnabled, boolean dimensionWhitelisted) {
		return configEnabled && worldEnabled && dimensionWhitelisted;
	}
}
