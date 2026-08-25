package dev.dangeroni.renewablelava.mixin;

import dev.dangeroni.renewablelava.rule.RenewableLavaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
    @Shadow
    protected abstract void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state);

    @ModifyConstant(method = "getNewLiquid", constant = @Constant(intValue = 2))
    private int renewable_lava$requiredSourceNeighbours(int original) {
        FlowingFluid fluid = (FlowingFluid) (Object) this;
        return fluid instanceof LavaFluid ? RenewableLavaRules.getRequiredSourceNeighbours() : original;
    }

    @Redirect(
        method = "getNewLiquid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/event/EventHooks;canCreateFluidSource(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
        )
    )
    private boolean renewable_lava$allowLavaSourceConversion(ServerLevel level, BlockPos pos, BlockState state) {
        FlowingFluid fluid = (FlowingFluid) (Object) this;
        return fluid instanceof LavaFluid ? RenewableLavaRules.isLavaSourceConversionEnabled(level) : EventHooks.canCreateFluidSource(level, pos, state);
    }

    @Inject(method = "spreadTo", at = @At("HEAD"), cancellable = true)
    private void renewable_lava$promoteSpreadTargetToSource(
        LevelAccessor level,
        BlockPos pos,
        BlockState state,
        Direction direction,
        FluidState target,
        CallbackInfo ci
    ) {
        FlowingFluid fluid = (FlowingFluid) (Object) this;
        if (!(fluid instanceof LavaFluid) || !(level instanceof ServerLevel serverLevel) || target.isSource() || !target.getType().isSame(fluid)) {
            return;
        }

        if (!RenewableLavaRules.shouldConvertToSource(fluid, serverLevel, pos)) {
            return;
        }

        FluidState source = fluid.getSource(false);
        if (state.getBlock() instanceof LiquidBlockContainer container) {
            container.placeLiquid(level, pos, state, source);
        } else {
            if (!state.isAir()) {
                this.beforeDestroyingBlock(level, pos, state);
            }

            level.setBlock(pos, source.createLegacyBlock(), 3);
        }

        ci.cancel();
    }
}
