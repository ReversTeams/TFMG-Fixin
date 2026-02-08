package net.reversteam.tfmgpatch.mixin;

import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GoggleOverlayRenderer.class, remap = false, priority = 2000)
public class GoggleOverlayRendererGuardMixin {
    @Unique
    private static final ThreadLocal<HitResult> tfmg$originalHit = new ThreadLocal<>();

    @Inject(method = "renderOverlay", at = @At("HEAD"))
    private static void tfmg$guardNonBlockHit(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height,
                                             CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult)) {
            tfmg$originalHit.set(hit);
            Vec3 location = hit == null ? Vec3.ZERO : hit.getLocation();
            mc.hitResult = BlockHitResult.miss(location, Direction.UP, BlockPos.ZERO);
        } else {
            tfmg$originalHit.remove();
        }
    }

    @Inject(method = "renderOverlay", at = @At("RETURN"))
    private static void tfmg$restoreHitResult(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height,
                                              CallbackInfo ci) {
        HitResult original = tfmg$originalHit.get();
        if (original != null) {
            Minecraft.getInstance().hitResult = original;
            tfmg$originalHit.remove();
        }
    }

    @Redirect(
            method = "handler$jfp000$tfmg$renderOverlay",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"
            ),
            remap = true,
            require = 0
    )
    private static HitResult tfmg$guardTfmGHandlerHitResult(Minecraft mc) {
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult) {
            return hit;
        }
        Vec3 location = hit == null ? Vec3.ZERO : hit.getLocation();
        return BlockHitResult.miss(location, Direction.UP, BlockPos.ZERO);
    }
}
