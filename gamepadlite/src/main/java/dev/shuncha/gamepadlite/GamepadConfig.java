package dev.shuncha.gamepadlite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.sdl.SDLGamepad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Gamepad Liteの設定。config/gamepadlite.jsonに保存される */
public final class GamepadConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("GamepadLite");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("gamepadlite.json");

    public boolean invertRightStickY = false;

    // リマップ可能な論理ボタン(値はSDL_GAMEPAD_BUTTON_*定数)。既定値は現在の物理配置と同じ
    public int buttonJumpOrClose = SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH;   // A: ジャンプ / 画面を閉じる
    public int buttonPrimary = SDLGamepad.SDL_GAMEPAD_BUTTON_EAST;        // B: 落とす / 決定(つかむ・置く)
    public int buttonSwapOffhand = SDLGamepad.SDL_GAMEPAD_BUTTON_WEST;    // X: オフハンド交換
    public int buttonInventory = SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH;     // Y: インベントリ開閉
    public int buttonPrev = SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER;  // LB: ホットバー/スロット 前へ
    public int buttonNext = SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER; // RB: ホットバー/スロット 次へ

    private static GamepadConfig instance;

    public static GamepadConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static GamepadConfig load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader r = Files.newBufferedReader(PATH)) {
                    GamepadConfig cfg = GSON.fromJson(r, GamepadConfig.class);
                    if (cfg != null) return cfg;
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("設定ファイルの読み込みに失敗したため、既定値を使用します", e);
        }
        return new GamepadConfig();
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            LOGGER.warn("設定ファイルの保存に失敗しました", e);
        }
    }
}