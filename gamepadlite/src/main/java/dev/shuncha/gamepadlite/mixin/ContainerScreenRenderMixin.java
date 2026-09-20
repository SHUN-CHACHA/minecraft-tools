package dev.shuncha.gamepadlite.mixin;

import dev.shuncha.gamepadlite.GamepadManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenRenderMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void gamepadlite$drawSlotHighlight(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        GamepadManager.renderSlotHighlight((AbstractContainerScreen<?>) (Object) this, graphics);
    }
}