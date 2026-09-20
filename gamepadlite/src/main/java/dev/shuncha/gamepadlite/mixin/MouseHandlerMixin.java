package dev.shuncha.gamepadlite.mixin;

import dev.shuncha.gamepadlite.GamepadManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    @Shadow private boolean mouseGrabbed;

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void gamepadlite$addLook(CallbackInfo ci) {
        if (!this.mouseGrabbed) return;
        if (GamepadManager.computeLook()) {
            this.accumulatedDX += GamepadManager.lookDx();
            this.accumulatedDY += GamepadManager.lookDy();
        }
    }
}