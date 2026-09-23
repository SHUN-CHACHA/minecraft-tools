package dev.shuncha.malilibime;

/**
 * MaLiLib系MODのクラスかどうかをクラス名で判定する。
 * MaLiLibへのビルド依存を持たないための仕組み。
 */
public final class MalilibCompat {
    private static final String PREFIX = "fi.dy.masa.";

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

    private MalilibCompat() {
    }

    public static boolean isMalilibWidget(Object obj) {
        return obj != null && CACHE.get(obj.getClass());
    }
}