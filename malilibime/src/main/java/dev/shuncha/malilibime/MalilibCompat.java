package dev.shuncha.malilibime;

import net.minecraft.client.gui.components.EditBox;

import java.lang.ref.WeakReference;

/**
 * - MaLiLib系MODのクラスかどうかをクラス名で判定する(MaLiLibへのビルド依存を持たないため)。
 * - 「いまフォーカス中で、直近に描画されたMaLiLibの入力欄」を記録する。
 *   変換中文字の転送先として使う(26.2/26.3共通。TextInputManagerの内部実装に依存しないため)。
 * - 「いまMaLiLibの入力欄を描画中か」の目印を持つ(変換中文字オーバーレイの包み直しに使う)。
 */
public final class MalilibCompat {
    private static final String PREFIX = "fi.dy.masa.";

    /** この時間より長く描画されていない欄は、画面が閉じられたものとみなす */
    private static final long STALE_NANOS = 250_000_000L;

    private static final ClassValue<Boolean> CACHE = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                if (c.getName().startsWith(PREFIX)) {
                    return true;
                }
            }
            return false;
        }
    };

    private static WeakReference<EditBox> activeField = new WeakReference<>(null);
    private static long lastSeenNanos;
    private static boolean renderingMalilibField;

    private MalilibCompat() {
    }

    public static boolean isMalilibWidget(Object obj) {
        return obj != null && CACHE.get(obj.getClass());
    }

    /** フォーカス中のMaLiLib入力欄が描画されたときに呼ぶ */
    public static void markActive(EditBox box) {
        if (activeField.get() != box) {
            activeField = new WeakReference<>(box);
        }
        lastSeenNanos = System.nanoTime();
    }

    /** 変換中文字の転送先。該当なしならnull */
    public static EditBox getActiveField() {
        EditBox box = activeField.get();
        if (box == null || !box.isFocused()) {
            return null;
        }
        if (System.nanoTime() - lastSeenNanos > STALE_NANOS) {
            return null;
        }
        return box;
    }

    public static void setRenderingMalilibField(boolean value) {
        renderingMalilibField = value;
    }

    public static boolean isRenderingMalilibField() {
        return renderingMalilibField;
    }
}