package dev.shuncha.malilibime.mixin;

import dev.shuncha.malilibime.MalilibCompat;
import dev.shuncha.malilibime.TopStratumOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * - MaLiLibの入力欄が登録する変換中文字オーバーレイを、新しい層で描くよう包み直す(ボタン等より前面に出す)。
 * - MaLiLibは画面によって、毎フレーム新しく作る独自の描画オブジェクト(GuiContext)で入力欄を描く。
 *   その場合、オーバーレイがGuiContext側に登録され、バニラが後回し描画(extractDeferredElements)をする
 *   本来の描画オブジェクトに届かないため、預かっておいて後回し描画の直前に引き渡す。
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {

    @Shadow
    private Renderable preeditOverlay;

    @Unique
    private static Renderable malilibime$pendingOverlay;

    @ModifyVariable(method = "setPreeditOverlay", at = @At("HEAD"), argsOnly = true)
    private Renderable malilibime$wrapPreeditOverlay(Renderable overlay) {
        if (overlay == null) {
            return null;
        }

        Renderable result = overlay;
        if (MalilibCompat.isRenderingMalilibField() && !(overlay instanceof TopStratumOverlay)) {
            result = new TopStratumOverlay(overlay);
        }

        if (MalilibCompat.isMalilibWidget(this)) {
            malilibime$pendingOverlay = result;
        }
        return result;
    }

    @Inject(method = "extractDeferredElements", at = @At("HEAD"))
    private void malilibime$adoptPreeditOverlay(int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Renderable pending = malilibime$pendingOverlay;
        malilibime$pendingOverlay = null;

        if (this.preeditOverlay == null && pending != null) {
            this.preeditOverlay = pending;
        }
    }
}