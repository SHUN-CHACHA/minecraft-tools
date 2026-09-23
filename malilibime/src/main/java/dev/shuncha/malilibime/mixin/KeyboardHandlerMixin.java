package dev.shuncha.malilibime.mixin;

import dev.shuncha.malilibime.MalilibCompat;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.PreeditEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 変換中の文字(PreeditEvent)は通常、画面のバニラ上のフォーカス要素へ配送される。
 * MaLiLibのテキスト欄はそこに登録されていないため、画面宛てのイベントで、
 * フォーカス中のMaLiLibの欄が直近に描画されていれば、その欄へ直接届ける。
 * (個々の欄宛てのイベント=フォーカス変更時のクリア通知などはそのまま通す)
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "submitPreeditEvent", at = @At("HEAD"), cancellable = true)
    private static void malilibime$redirectPreedit(GuiEventListener listener, PreeditEvent event, CallbackInfo ci) {
        if (!(listener instanceof Screen)) {
            return;
        }

        EditBox target = MalilibCompat.getActiveField();
        if (target == null) {
            return;
        }

        target.preeditUpdated(event);
        ci.cancel();
    }
}