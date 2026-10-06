package dev.shuncha.headfireworkpaper;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Paper版の多言語対応(日本語/英語)をまとめて扱うユーティリティクラス。
 *
 * プレイヤーへのチャットメッセージ・GUIの文字列は、送信相手が持つクライアントの
 * 言語設定({@link Player#locale()})に応じて自動的に日本語/英語を切り替える。
 * コンソール(CommandSenderがPlayerでない場合)は常に日本語のまま。
 *
 * クラフトしたアイテム(花火の星・ロケット)の表示名だけは例外で、アイテムのNBTに
 * 一度焼き込まれる固定文字列のため、閲覧者ごとに動的な切り替えはできない。そのため、
 * クラフトした本人のクライアント言語を使って表示名を決定している(呼び出し側で対応)。
 *
 * 現時点では日本語(ja)・英語(en)の2言語のみ対応。日本語以外のクライアント言語
 * (例: 中国語・韓国語)の場合は英語にフォールバックする。
 */
public final class Lang {

    private Lang() {
    }

    private static final Map<String, String> JA = new HashMap<>();
    private static final Map<String, String> EN = new HashMap<>();

    private static void put(String key, String ja, String en) {
        JA.put(key, ja);
        EN.put(key, en);
    }

    static {
        // --- 共通エラー ---
        put("error.player_only", "このコマンドはプレイヤーのみ実行できます。",
                "This command can only be used by a player.");
        put("error.no_permission", "このコマンドを使う権限がありません。",
                "You don't have permission to use this command.");
        put("error.specify_item", "項目を指定してください: %s",
                "Please specify an item: %s");
        put("error.unknown_item", "不明な項目です: %s",
                "Unknown item: %s");
        put("error.unknown_shape", "不明な形状です: %s (使用可能: %s)",
                "Unknown shape: %s (available: %s)");
        put("error.myface_unknown_direction", "不明な方角です: %s (使用可能: %s / show / reset)",
                "Unknown direction: %s (available: %s / show / reset)");
        put("error.config_unknown_direction", "不明な方角です: %s (使用可能: %s)",
                "Unknown direction: %s (available: %s)");
        put("error.specify_number", "数値を指定してください: %s",
                "Please enter a number: %s");
        put("error.specify_integer", "整数を指定してください: %s",
                "Please enter an integer: %s");
        put("error.specify_angle_number", "角度は数値で指定してください。",
                "Please enter angles as numbers.");
        put("error.specify_on_off", "onまたはoffを指定してください: %s",
                "Please specify on or off: %s");

        // --- 使用法 ---
        put("usage.main", "使用法: /headfirework config <項目> <値> / /headfirework gui / "
                        + "/headfirework myface <方角> / /headfirework mygui / /headfirework testhead <pitch> <roll> <yaw>",
                "Usage: /headfirework config <item> <value> / /headfirework gui / "
                        + "/headfirework myface <direction> / /headfirework mygui / /headfirework testhead <pitch> <roll> <yaw>");
        put("usage.testhead", "使用法: /headfirework testhead <pitch> <roll> <yaw> (角度は度数法)",
                "Usage: /headfirework testhead <pitch> <roll> <yaw> (angles in degrees)");
        put("usage.myface", "使用法: /headfirework myface <north|south|east|west> / show / reset",
                "Usage: /headfirework myface <north|south|east|west> / show / reset");
        put("usage.config_scale", "使用法: /headfirework config scale <形状> <値>",
                "Usage: /headfirework config scale <shape> <value>");
        put("usage.config_facing", "使用法: /headfirework config facing <north|south|east|west>",
                "Usage: /headfirework config facing <north|south|east|west>");
        put("usage.config_update_check", "使用法: /headfirework config update_check <on|off>",
                "Usage: /headfirework config update_check <on|off>");

        // --- myface(参加者本人用) ---
        put("myface.show", "あなたの花火の顔の向き設定: %s",
                "Your firework face direction setting: %s");
        put("myface.show_unset", "未設定(サーバーのデフォルト値「%s」を使用中)",
                "Not set (using server default: %s)");
        put("myface.reset", "顔の向き設定をサーバーのデフォルト値に戻しました。",
                "Your face direction setting has been reset to the server default.");
        put("myface.set", "あなたの花火に映る顔の向きを%sに設定しました。",
                "Set the direction your face shows in your fireworks to %s.");

        // --- config(管理者用) ---
        put("config.scale_reset", "%sのサイズをデフォルトに戻しました。",
                "Reset %s size to default.");
        put("config.scale_set", "%sのサイズを%sに設定しました。",
                "Set %s size to %s.");
        put("config.int_reset", "%sをデフォルトに戻しました。",
                "Reset %s to default.");
        put("config.int_set", "%sを%stickに設定しました。",
                "Set %s to %s ticks.");
        put("config.facing_set", "顔の向きを%sに設定しました。",
                "Set the face direction to %s.");
        put("config.update_check_on", "起動時の更新チェックを有効にしました(次回起動時から反映されます)。",
                "Enabled the startup update check (effective from next server start).");
        put("config.update_check_off", "起動時の更新チェックを無効にしました(次回起動時から反映されます)。",
                "Disabled the startup update check (effective from next server start).");
        put("config.show_header", "=== HeadFirework 現在の設定 ===",
                "=== HeadFirework Current Settings ===");

        // --- testhead(デバッグ用) ---
        put("testhead.spawned", "pitch=%s roll=%s yaw=%s で頭を出しました(5秒で消えます)",
                "Spawned a head with pitch=%s roll=%s yaw=%s (disappears in 5 seconds)");

        // --- 項目ラベル(config系メッセージ・GUI共用) ---
        put("label.display_duration", "表示時間", "Display Duration");
        put("label.animation_duration", "拡大アニメーション時間", "Animation Duration");
        put("label.fade_duration", "フェードアウト時間", "Fade Duration");

        // --- GUI ---
        put("gui.admin.title", "HeadFirework 設定", "HeadFirework Settings");
        put("gui.personal.title", "HeadFirework 自分の設定", "HeadFirework My Settings");
        put("gui.label.small_ball", "小玉サイズ", "Small Ball Size");
        put("gui.label.large_ball", "大玉サイズ", "Large Ball Size");
        put("gui.label.star_burst", "星型/バーストサイズ", "Star/Burst Size");
        put("gui.label.facing_default", "サーバー全体のデフォルトの向き", "Server Default Direction");
        put("gui.label.facing_personal", "自分の花火の顔の向き", "My Firework Face Direction");
        put("gui.hint.click_cycle", "クリックで切り替え(北→東→南→西)", "Click to cycle (North→East→South→West)");
        put("gui.status.personal_active", "個人設定が有効です", "Personal setting is active");
        put("gui.status.using_default", "サーバーのデフォルト値を使用中です", "Using the server default");
        put("gui.reset_to_default", "サーバーのデフォルト値に戻す", "Reset to server default");
        put("gui.reset_to_default_lore", "個人設定を削除します", "Removes your personal setting");
        put("gui.current_value", "現在値: ", "Current: ");
        put("gui.reset", "リセット", "Reset");
        put("gui.close", "閉じる", "Close");

        // --- 方角 ---
        put("facing.north", "北 (north)", "North");
        put("facing.east", "東 (east)", "East");
        put("facing.south", "南 (south)", "South");
        put("facing.west", "西 (west)", "West");

        // --- 更新通知 ---
        put("update.new_version", "新しいバージョン %s が公開されています(現在: v%s)。",
                "A new version %s is available (current: v%s).");
        put("update.download", "ダウンロード: %s", "Download: %s");

        // --- クラフトしたアイテムの表示名(クラフトした本人の言語を使用) ---
        put("item.star_of", "%sの花火の星", "%s's Firework Star");
        put("item.rocket_of", "%sの花火", "%s's Firework");
    }

        /** 送信相手の言語設定(日本語/英語)を判定する。コンソール等は常に日本語扱い。 */
    public static boolean isEnglish(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        Locale locale = player.locale();
        return locale != null && !"ja".equalsIgnoreCase(locale.getLanguage());
    }

    /** sender(プレイヤーまたはコンソール)の言語に応じたメッセージを組み立てる。 */
    public static String msg(CommandSender sender, String key, Object... args) {
        Map<String, String> table = isEnglish(sender) ? EN : JA;
        String template = table.getOrDefault(key, key);
        return args.length == 0 ? template : String.format(template, args);
    }
}