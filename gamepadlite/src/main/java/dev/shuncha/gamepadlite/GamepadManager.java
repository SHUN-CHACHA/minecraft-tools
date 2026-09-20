package dev.shuncha.gamepadlite;

import dev.shuncha.gamepadlite.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

public final class GamepadManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("GamepadLite");

    private static final float STICK_DEADZONE = 0.2f;
    private static final float TRIGGER_THRESHOLD = 0.5f;
    /** 右スティック全倒し時の視点速度(マウス換算ピクセル/秒)。感度設定も掛かる */
    private static final double LOOK_SPEED = 900.0;
    private static final int RETRY_TICKS = 40;

    private static boolean sdlReady;
    private static boolean sdlFailed;
    private static long pad;
    private static int retry;
    private static long lastLookNanos = System.nanoTime();

    private static boolean active;
    private static float moveX, moveY, lookX, lookY;
    private static boolean jumpHeld, sneakHeld, sprintHeld;
    private static double lookDx, lookDy;

    private static boolean prevRt, prevLt, prevB, prevX, prevY, prevLb, prevRb;

    private GamepadManager() {}

    // ---- Mixinから参照する値 ----
    public static boolean isActive() { return active && pad != 0L; }
    public static float moveX() { return moveX; }
    public static float moveY() { return moveY; }
    public static boolean jumpHeld() { return jumpHeld; }
    public static boolean sneakHeld() { return sneakHeld; }
    public static boolean sprintHeld() { return sprintHeld; }
    public static double lookDx() { return lookDx; }
    public static double lookDy() { return lookDy; }

    // ---- 初期化・終了 ----
    private static void initSdl() {
        if (sdlReady || sdlFailed) return;
        if (!SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_GAMEPAD)) {
            sdlFailed = true;
            LOGGER.error("SDL gamepad subsystem init failed");
            return;
        }
        // 本体のイベントキューにゲームパッドイベントを流さず、こちらで直接ポーリングする
        SDLGamepad.SDL_SetGamepadEventsEnabled(false);
        sdlReady = true;
    }

    public static void shutdown() {
        if (pad != 0L) {
            SDLGamepad.SDL_CloseGamepad(pad);
            pad = 0L;
        }
    }

    private static void ensurePad() {
        if (pad != 0L) return;
        if (retry-- > 0) return;
        retry = RETRY_TICKS;
        SDLGamepad.SDL_UpdateGamepads();
        if (!SDLGamepad.SDL_HasGamepad()) return;
        IntBuffer ids = SDLGamepad.SDL_GetGamepads();
        if (ids == null || !ids.hasRemaining()) return;
        long p = SDLGamepad.SDL_OpenGamepad(ids.get(0));
        if (p == 0L) return;
        pad = p;
        LOGGER.info("Gamepad connected: {}", SDLGamepad.SDL_GetGamepadName(p));
    }

    // ---- 入力読み取り ----
    private static float axis(int a) {
        return Math.max(-1f, SDLGamepad.SDL_GetGamepadAxis(pad, a) / 32767f);
    }

    private static boolean btn(int b) {
        return SDLGamepad.SDL_GetGamepadButton(pad, b);
    }

    /** 円形デッドゾーン処理。out[0]=x, out[1]=y */
    private static void radial(float x, float y, float[] out) {
        float mag = (float) Math.sqrt(x * x + y * y);
        if (mag < STICK_DEADZONE) {
            out[0] = 0f;
            out[1] = 0f;
            return;
        }
        float scaled = Math.min(1f, (mag - STICK_DEADZONE) / (1f - STICK_DEADZONE));
        out[0] = x / mag * scaled;
        out[1] = y / mag * scaled;
    }

    private static final float[] TMP = new float[2];

    private static void updateSticks() {
        SDLGamepad.SDL_UpdateGamepads();
        if (!SDLGamepad.SDL_GamepadConnected(pad)) {
            LOGGER.info("Gamepad disconnected");
            shutdown();
            return;
        }
        radial(axis(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX), axis(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY), TMP);
        moveX = TMP[0];
        moveY = TMP[1];
        radial(axis(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX), axis(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY), TMP);
        lookX = TMP[0];
        lookY = TMP[1];
    }

    /** 毎フレーム(MouseHandlerのMixinから)呼ぶ。視点移動量をlookDx/lookDyに入れ、動かすならtrue */
    public static boolean computeLook() {
        long now = System.nanoTime();
        double dt = (now - lastLookNanos) / 1.0e9;
        lastLookNanos = now;
        if (!isActive()) return false;
        if (dt > 0.1) dt = 0.1;
        updateSticks();
        if (pad == 0L) return false;
        if (lookX == 0f && lookY == 0f) return false;
        // 二乗カーブ: 小さな倒しで細かく、大きく倒すと速く
        double cx = lookX * Math.abs(lookX);
        double cy = lookY * Math.abs(lookY);
        lookDx = cx * LOOK_SPEED * dt;
        lookDy = cy * LOOK_SPEED * dt;
        return true;
    }

    // ---- 毎tick処理(ボタン類) ----
    public static void tick(Minecraft mc) {
        initSdl();
        if (!sdlReady) return;
        ensurePad();
        if (pad == 0L) {
            resetState(mc);
            return;
        }
        updateSticks();
        if (pad == 0L) {
            resetState(mc);
            return;
        }

        Options o = mc.options;
        // 26.3では画面の管理がMinecraftからGuiに移動している
        boolean noScreen = mc.gui.screen() == null;
        boolean gameActive = mc.player != null && noScreen;
        active = gameActive;

        boolean b = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_EAST);
        boolean x = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_WEST);
        boolean y = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH);
        boolean lb = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER);
        boolean rb = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER);
        boolean rt = axis(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER) > TRIGGER_THRESHOLD;
        boolean lt = axis(SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER) > TRIGGER_THRESHOLD;

        if (gameActive) {
            jumpHeld = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH);
            sprintHeld = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK);
            sneakHeld = btn(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK);

            hold(o.keyAttack, rt, prevRt);
            hold(o.keyUse, lt, prevLt);
            if (b && !prevB) click(o.keyDrop);
            if (x && !prevX) click(o.keySwapOffhand);
            if (y && !prevY) click(o.keyInventory);

            Inventory inv = mc.player.getInventory();
            if (lb && !prevLb) inv.setSelectedSlot(Math.floorMod(inv.getSelectedSlot() - 1, 9));
            if (rb && !prevRb) inv.setSelectedSlot(Math.floorMod(inv.getSelectedSlot() + 1, 9));
        } else {
            jumpHeld = false;
            sprintHeld = false;
            sneakHeld = false;
            hold(o.keyAttack, false, prevRt);
            hold(o.keyUse, false, prevLt);
            // 画面(インベントリ等)を開いている間はBで閉じる
            if (!noScreen && b && !prevB) mc.gui.setScreen(null);
        }

        prevRt = rt;
        prevLt = lt;
        prevB = b;
        prevX = x;
        prevY = y;
        prevLb = lb;
        prevRb = rb;
    }

    private static void resetState(Minecraft mc) {
        active = false;
        moveX = moveY = lookX = lookY = 0f;
        jumpHeld = sneakHeld = sprintHeld = false;
        if (mc.options != null) {
            hold(mc.options.keyAttack, false, prevRt);
            hold(mc.options.keyUse, false, prevLt);
        }
        prevRt = prevLt = prevB = prevX = prevY = prevLb = prevRb = false;
    }

    // ---- KeyMapping操作 ----
    private static void click(KeyMapping km) {
        KeyMappingAccessor acc = (KeyMappingAccessor) km;
        acc.gamepadlite$setClickCount(acc.gamepadlite$getClickCount() + 1);
    }

    private static void hold(KeyMapping km, boolean now, boolean prev) {
        if (now) {
            if (!prev) click(km);
            km.setDown(true);
        } else if (prev) {
            km.setDown(false);
        }
    }
}