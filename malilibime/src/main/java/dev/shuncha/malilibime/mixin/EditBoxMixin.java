package dev.shuncha.malilibime.mixin;

import com.mojang.blaze3d.platform.TextInputManager;
import dev.shuncha.malilibime.MalilibCompat;
import dev.shuncha.malilibime.TopStratumOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * - フォーカス中のMaLiLibテキスト欄について、IMEがオンかつ自分が入力先になっているかを毎フレーム確認し、
 *   画面切り替え時などにオフにされていたら再度オンにする。
 * - MaLiLibの欄では、変換中文字のオーバーレイを新しい層で描くよう包み直す。
 */
@Mixin(EditBox.class)
public abstract class EditBoxMixin {

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void malilibime$ensureTextInput(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        EditBox self = (EditBox) (Object) this;

        if (!self.isFocused() || !self.canConsumeInput() || !MalilibCompat.isMalilibWidget(self)) {
            return;
        }

        TextInputManager manager = Minecraft.getInstance().textInputManager();
        TextInputManagerAccessor accessor = (TextInputManagerAccessor) manager;

        if (accessor.malilibime$getOwner() != self || !accessor.malilibime$isTextInputEnabled()) {
            manager.startTextInput(self);
        }
    }

    @Redirect(
            method = "extractWidgetRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setPreeditOverlay(Lnet/minecraft/client/gui/components/Renderable;)V"
            )
    )
    private void malilibime$wrapPreeditOverlay(GuiGraphicsExtractor graphics, Renderable overlay) {
        if (overlay != null && MalilibCompat.isMalilibWidget(this)) {
            graphics.setPreeditOverlay(new TopStratumOverlay(overlay));
        } else {
            graphics.setPreeditOverlay(overlay);
        }
    }
}