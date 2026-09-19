package dev.shuncha.efp;

import dev.shuncha.efp.gui.EfpGuiListener;
import dev.shuncha.efp.model.BaseLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EfpCommand implements CommandExecutor, TabCompleter {

    private final EfpPlugin plugin;

    public EfpCommand(EfpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("gui")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("このコマンドはゲーム内でのみ実行できます", NamedTextColor.RED));
                return true;
            }
            String environment = player.getWorld().getEnvironment().name();
            List<BaseLocation> publicLocations = plugin.getLocationManager().getPublicLocationsByEnvironment(environment);
            player.openInventory(EfpGuiListener.build(publicLocations, environment));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはゲーム内でのみ実行できます", NamedTextColor.RED));
            return true;
        }

        switch (sub) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "rename" -> handleRename(player, args);
            case "list" -> handleList(player);
            case "public" -> handleVisibility(player, args, true);
            case "private" -> handleVisibility(player, args, false);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("使い方: /efp add <拠点名> [public|private]", NamedTextColor.RED));
            return;
        }
        String name = args[1];
        // デフォルトは公開。明示的に private を指定した場合のみ非公開にする
        boolean pub = !(args.length >= 3 && args[2].equalsIgnoreCase("private"));
        Location loc = player.getLocation();
        String environment = player.getWorld().getEnvironment().name();
        BaseLocation added = plugin.getLocationManager().addOrUpdate(
                player.getUniqueId(), player.getName(), name,
                loc.getWorld().getName(), environment, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), pub);
        player.sendMessage(Component.text("「" + name + "」を登録しました (" +
                added.getDimensionLabel() + " / " + (pub ? "公開" : "非公開") + ")", NamedTextColor.GREEN));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("使い方: /efp remove <拠点名>", NamedTextColor.RED));
            return;
        }
        boolean removed = plugin.getLocationManager().remove(player.getUniqueId(), args[1]);
        player.sendMessage(removed
                ? Component.text("「" + args[1] + "」を削除しました", NamedTextColor.GREEN)
                : Component.text("その名前の拠点は見つかりませんでした", NamedTextColor.RED));
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("使い方: /efp rename <旧名> <新名>", NamedTextColor.RED));
            return;
        }
        String oldName = args[1];
        String newName = args[2];
        BaseLocation existing = plugin.getLocationManager().findByOwnerAndName(player.getUniqueId(), oldName);
        if (existing == null) {
            player.sendMessage(Component.text("その名前の拠点は見つかりませんでした", NamedTextColor.RED));
            return;
        }
        boolean ok = plugin.getLocationManager().rename(player.getUniqueId(), oldName, newName);
        player.sendMessage(ok
                ? Component.text("「" + oldName + "」を「" + newName + "」に変更しました", NamedTextColor.GREEN)
                : Component.text("「" + newName + "」は既に使われています", NamedTextColor.RED));
    }

    private void handleList(Player player) {
        List<BaseLocation> mine = plugin.getLocationManager().getByOwner(player.getUniqueId());
        if (mine.isEmpty()) {
            player.sendMessage(Component.text("登録済みの拠点はありません", NamedTextColor.GRAY));
            return;
        }
        player.sendMessage(Component.text("=== あなたの拠点一覧 ===", NamedTextColor.AQUA));
        for (BaseLocation loc : mine) {
            player.sendMessage(Component.text(
                    String.format("・%s [%s] (%s) X:%d Y:%d Z:%d [%s]",
                            loc.getName(), loc.getDimensionLabel(), loc.getWorld(),
                            loc.getX(), loc.getY(), loc.getZ(),
                            loc.isPublic() ? "公開" : "非公開"),
                    NamedTextColor.WHITE));
        }
    }

    private void handleVisibility(Player player, String[] args, boolean pub) {
        if (args.length < 2) {
            player.sendMessage(Component.text("使い方: /efp " + (pub ? "public" : "private") + " <拠点名>", NamedTextColor.RED));
            return;
        }
        boolean ok = plugin.getLocationManager().setPublic(player.getUniqueId(), args[1], pub);
        player.sendMessage(ok
                ? Component.text("「" + args[1] + "」を" + (pub ? "公開" : "非公開") + "にしました", NamedTextColor.GREEN)
                : Component.text("その名前の拠点は見つかりませんでした", NamedTextColor.RED));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== EFP コマンド一覧 ===", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/efp add <名前> [public|private] - 現在地を拠点として登録(デフォルト:公開、ディメンションは自動記録)", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/efp remove <名前> - 拠点を削除", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/efp rename <旧名> <新名> - 拠点名を変更", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/efp list - 自分の拠点一覧を表示", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/efp public <名前> / private <名前> - 公開設定を変更", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/efp gui - 今いるディメンションの公開拠点を座標マップで表示", NamedTextColor.WHITE));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(List.of("add", "remove", "rename", "list", "gui", "public", "private"));
            return filter(result, args[0]);
        }
        if (args.length == 2 && sender instanceof Player player) {
            String sub = args[0].toLowerCase();
            if (sub.equals("remove") || sub.equals("public") || sub.equals("private") || sub.equals("rename")) {
                List<String> names = plugin.getLocationManager().getByOwner(player.getUniqueId())
                        .stream().map(BaseLocation::getName).collect(Collectors.toList());
                return filter(names, args[1]);
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            return filter(List.of("public", "private"), args[2]);
        }
        return result;
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}