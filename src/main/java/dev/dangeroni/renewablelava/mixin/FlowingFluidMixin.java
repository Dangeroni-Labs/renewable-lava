package dev.dangeroni.renewablelava.mixin;

import dev.dangeroni.renewablelava.rule.RenewableLavaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
	@Shadow
	protected abstract boolean canConvertToSource(ServerLevel level);

	// Hook the exact vanilla source-conversion decision so lava reuses the normal
	// neighbour, support, and update semantics from FlowingFluid#getNewLiquid.
	@ModifyConstant(method = "getNewLiquid", constant = @Constant(intValue = 2))
	private int renewable_lava$requiredSourceNeighbours(int original) {
		FlowingFluid fluid = (FlowingFluid)(Object)this;
		return fluid instanceof LavaFluid ? RenewableLavaRules.getRequiredSourceNeighbours() : original;
	}

	@Redirect(
		method = "getNewLiquid",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/material/FlowingFluid;canConvertToSource(Lnet/minecraft/server/level/ServerLevel;)Z"
		)
	)
	private boolean renewable_lava$allowLavaSourceConversion(FlowingFluid fluid, ServerLevel level) {
		return fluid instanceof LavaFluid ? RenewableLavaRules.isLavaSourceConversionEnabled(level) : this.canConvertToSource(level);
	}
}
