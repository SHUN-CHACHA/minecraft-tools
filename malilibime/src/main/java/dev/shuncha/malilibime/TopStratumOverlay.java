package dev.shuncha.malilibime;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

/**
 * 変換中文字のオーバーレイを、描画直前に新しい層(ストラタム)へ切り替えてから描く。
 * MaLiLibのボタン等より確実に前面に出すため。
 */
public final class TopStratumOverlay implements Renderable {
    private final Renderable delegate;

    public TopStratumOverlay(Renderable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.nextStratum();
        this.delegate.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}