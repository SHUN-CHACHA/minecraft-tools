package dev.shuncha.headfireworkpaper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * headfirework.admin権限を持つプレイヤー(OP)がサーバーに参加したとき、
 * 新しいバージョンが公開されていればチャットで知らせるリスナー。
 *
 * サーバーコンソールのログとは別に、実際にプレイしているOPにもゲーム内で
 * 気づいてもらうための通知。UpdateCheckerの非同期チェックが起動時に完了していない
 * (サーバー起動直後にOPが参加した)場合は何も表示されない点に注意。
 *
 * 通知文は参加したOP本人のクライアント言語設定({@link Lang})に応じて切り替わる。
 */
public class UpdateNotifyListener implements Listener {

    private final HeadFireworkPaperPlugin plugin;

    public UpdateNotifyListener(HeadFireworkPaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("headfirework.admin")) {
            return;
        }

        UpdateChecker checker = plugin.getUpdateChecker();
        if (checker == null || !checker.isUpdateAvailable()) {
            return;
        }

        String currentVersion = plugin.getDescription().getVersion();
        player.sendMessage(ChatColor.GOLD + "[HeadFirework] " + ChatColor.YELLOW
                + Lang.msg(player, "update.new_version", ChatColor.AQUA + checker.getLatestVersion() + ChatColor.YELLOW, currentVersion));
        if (checker.getLatestReleaseUrl() != null) {
            player.sendMessage(ChatColor.GOLD + "[HeadFirework] " + ChatColor.YELLOW
                    + Lang.msg(player, "update.download", ChatColor.AQUA + checker.getLatestReleaseUrl()));
        }
    }
}