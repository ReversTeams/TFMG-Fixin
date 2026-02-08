package net.reversteam.tfmgpatch.mixin;

import com.drmangotea.tfmg.worldgen.deposits.OilDepositFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OilDepositFeature.class, remap = false)
public class OilDepositFeatureMixin {
    @Unique
    private static final ThreadLocal<ChunkPos> tfmg$originChunk = new ThreadLocal<>();

    @Inject(method = { "place", "m_142674_" }, at = @At("HEAD"))
    private void tfmg$storeOriginChunk(FeaturePlaceContext<NoneFeatureConfiguration> context,
                                       CallbackInfoReturnable<Boolean> cir) {
        tfmg$originChunk.set(new ChunkPos(context.origin()));
    }

    @Inject(method = { "place", "m_142674_" }, at = @At("RETURN"))
    private void tfmg$clearOriginChunk(FeaturePlaceContext<NoneFeatureConfiguration> context,
                                       CallbackInfoReturnable<Boolean> cir) {
        tfmg$originChunk.remove();
    }

    @Redirect(method = "placeDeposit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            remap = true)
    private boolean tfmg$guardDepositSetBlock(WorldGenLevel level, BlockPos pos, BlockState state, int flags) {
        ChunkPos originChunk = tfmg$originChunk.get();
        if (originChunk != null && !originChunk.equals(new ChunkPos(pos))) {
            return false;
        }
        return level.setBlock(pos, state, flags);
    }
}
