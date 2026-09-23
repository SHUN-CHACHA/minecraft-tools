package dev.shuncha.malilibime.mixin;

import dev.shuncha.malilibime.MalilibCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MaLiLibは画面によって、毎フレーム新しく作る独自の描画オブジェクト(GuiContext)で入力欄を描く。
 * その場合、変換中文字のオーバーレイがGuiContext側に登録されてしまい、
 * バニラが後回し描画(extractDeferredElements)をする本来の描画オブジェクトに届かない。
 * そこで、MaLiLib側に登録されたオーバーレイを預かり、後回し描画の直前に引き渡す。
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {

    @Shadow
    private Renderable preeditOverlay;

    @Unique
    private static Renderable malilibime$pendingOverlay;

    @Inject(method = "setPreeditOverlay", at = @At("HEAD"))
    private void malilibime$capturePreeditOverlay(Renderable overlay, CallbackInfo ci) {
        if (overlay != null && MalilibCompat.isMalilibWidget(this)) {
            malilibime$pendingOverlay = overlay;
        }
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