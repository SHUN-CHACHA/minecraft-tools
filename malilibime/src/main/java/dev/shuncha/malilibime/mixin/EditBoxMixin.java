package dev.shuncha.malilibime.mixin;

import dev.shuncha.malilibime.MalilibCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * - MaLiLibの入力欄を描画している間、「MaLiLibの欄を描画中」の目印を立てる
 *   (GuiGraphicsExtractorMixin が変換中文字オーバーレイを包み直すのに使う)。
 * - フォーカス中のMaLiLibテキスト欄を「変換中文字の転送先」として記録する。
 * - IMEがオフにされていたら(MaLiLibが他の欄のフォーカスを外した時など)、
 *   バニラの Minecraft.onTextInputFocusChange で再度オンにする(26.2/26.3共通)。
 * ※ 古いMixinExtras(26.2環境の0.5.4など)で問題が出るため、@Redirect は使わない。
 */
@Mixin(EditBox.class)
public abstract class EditBoxMixin {

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void malilibime$beforeRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        EditBox self = (EditBox) (Object) this;
        boolean malilib = MalilibCompat.isMalilibWidget(self);
        MalilibCompat.setRenderingMalilibField(malilib);

        if (!malilib || !self.isFocused() || !self.canConsumeInput()) {
            return;
        }

        MalilibCompat.markActive(self);

        Minecraft mc = Minecraft.getInstance();
        if (!((TextInputManagerAccessor) mc.textInputManager()).malilibime$isTextInputEnabled()) {
            mc.onTextInputFocusChange(self, true);
        }
    }

    @Inject(method = "extractWidgetRenderState", at = @At("RETURN"))
    private void malilibime$afterRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        MalilibCompat.setRenderingMalilibField(false);
    }
}