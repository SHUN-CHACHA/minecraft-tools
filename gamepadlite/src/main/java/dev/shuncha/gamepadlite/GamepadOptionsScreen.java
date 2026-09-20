package dev.shuncha.gamepadlite;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GamepadOptionsScreen extends Screen {
    private final Screen parent;
    private final GamepadConfig config;

    public GamepadOptionsScreen(Screen parent) {
        super(Component.literal("Gamepad Lite 設定"));
        this.parent = parent;
        this.config = GamepadConfig.get();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;

        this.addRenderableWidget(CycleButton.onOffBuilder(config.invertRightStickY)
                .create(cx - 100, y, 200, 20, Component.literal("右スティックY軸反転"),
                        (btn, value) -> {
                            config.invertRightStickY = value;
                            config.save();
                        }));
        y += 28;

        y = addRebindRow(cx, y, "jumpOrClose");
        y = addRebindRow(cx, y, "primary");
        y = addRebindRow(cx, y, "swapOffhand");
        y = addRebindRow(cx, y, "inventory");
        y = addRebindRow(cx, y, "prev");
        y = addRebindRow(cx, y, "next");

        y += 10;
        this.addRenderableWidget(Button.builder(Component.literal("閉じる"), b -> {
                    config.save();
                    this.minecraft.gui.setScreen(parent);
                })
                .bounds(cx - 75, y, 150, 20)
                .build());
    }

    private int addRebindRow(int cx, int y, String role) {
        Button[] holder = new Button[1];
        Button button = Button.builder(Component.literal(rowLabel(role)), b -> startRebind(holder[0], role))
                .bounds(cx - 100, y, 200, 20)
                .build();
        holder[0] = button;
        this.addRenderableWidget(button);
        return y + 24;
    }

    private String rowLabel(String role) {
        return roleDisplayName(role) + ": " + GamepadManager.buttonLabel(getRoleValue(role));
    }

    private String roleDisplayName(String role) {
        return switch (role) {
            case "jumpOrClose" -> "ジャンプ/閉じる";
            case "primary" -> "決定/落とす";
            case "swapOffhand" -> "オフハンド交換";
            case "inventory" -> "インベントリ開閉";
            case "prev" -> "前へ";
            case "next" -> "次へ";
            default -> role;
        };
    }

    private int getRoleValue(String role) {
        return switch (role) {
            case "jumpOrClose" -> config.buttonJumpOrClose;
            case "primary" -> config.buttonPrimary;
            case "swapOffhand" -> config.buttonSwapOffhand;
            case "inventory" -> config.buttonInventory;
            case "prev" -> config.buttonPrev;
            case "next" -> config.buttonNext;
            default -> -1;
        };
    }

    private void setRoleValue(String role, int value) {
        switch (role) {
            case "jumpOrClose" -> config.buttonJumpOrClose = value;
            case "primary" -> config.buttonPrimary = value;
            case "swapOffhand" -> config.buttonSwapOffhand = value;
            case "inventory" -> config.buttonInventory = value;
            case "prev" -> config.buttonPrev = value;
            case "next" -> config.buttonNext = value;
            default -> {}
        }
    }

    private void startRebind(Button button, String role) {
        button.setMessage(Component.literal("ボタンを押してください..."));
        GamepadManager.startCapture(id -> {
            setRoleValue(role, id);
            config.save();
            button.setMessage(Component.literal(rowLabel(role)));
        });
    }

    @Override
    public void onClose() {
        config.save();
        this.minecraft.gui.setScreen(parent);
    }
}