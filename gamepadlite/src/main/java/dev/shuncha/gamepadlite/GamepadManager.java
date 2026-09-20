package dev.shuncha.gamepadlite;

import dev.shuncha.gamepadlite.mixin.ContainerScreenAccessor;
import dev.shuncha.gamepadlite.mixin.ContainerScreenInvoker;
import dev.shuncha.gamepadlite.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.List;
import java.util.function.IntConsumer;

public final class GamepadManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("GamepadLite");

    private static final float STICK_DEADZONE = 0.2f;
    private static final float TRIGGER_THRESHOLD = 0.5f;
    /** 右スティック全倒し時の視点速度(マウス換算ピクセル/秒)。感度設定も掛かる */
    private static final double LOOK_SPEED = 900.0;
    private static final int RETRY_TICKS = 40;
    /** 選択中スロットの枠の色(不透明の黄色) */
    private static final int SLOT_HIGHLIGHT_COLOR = 0xFFFFFF00;

    private static boolean sdlReady;
    private static boolean sdlFailed;
    private static long pad;
    private static int retry;
    private static long lastLookNanos = System.nanoTime();

    private static boolean active;
    private static float moveX, moveY, lookX, lookY;
    private static boolean jumpHeld, sneakHeld, sprintHeld;
    private static double lookDx, lookDy;

    private static boolean prevA, prevRt, prevLt, prevB, prevX, prevY, prevLb, prevRb;

    // インベントリ等の画面内でのスロット選択(LB/RBで移動、Bで決定)
    private static Object lastMenuRef;
    private static int selectedSlotIndex;

    // ボタン再割り当て(設定画面から呼ばれる)
    private static IntConsumer captureCallback;
    private static boolean[] prevAnyButton;

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

    // ---- 設定画面向け ----
    public static void startCapture(IntConsumer callback) {
        captureCallback = callback;
    }

    public static boolean isCapturing() {
        return captureCallback != null;
    }

    public static String buttonLabel(int id) {
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH) return "A";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_EAST) return "B";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_WEST) return "X";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH) return "Y";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER) return "LB";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER) return "RB";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK) return "L3";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK) return "R3";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_BACK) return "Back";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_START) return "Start";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE) return "Guide";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP) return "十字上";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN) return "十字下";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT) return "十字左";
        if (id == SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT) return "十字右";
        if (id < 0) return "-";
        return "Btn" + id;
    }

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
        if (b < 0) return false;
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
        if (GamepadConfig.get().invertRightStickY) {
            lookY = -lookY;
        }
    }

    /** ボタンの再割り当て待ち(設定画面のキャプチャ)を毎tick確認する */
    private static void pollCapture() {
        if (pad == 0L) return;
        if (prevAnyButton == null || prevAnyButton.length != SDLGamepad.SDL_GAMEPAD_BUTTON_COUNT) {
            prevAnyButton = new boolean[SDLGamepad.SDL_GAMEPAD_BUTTON_COUNT];
        }
        for (int id = 0; id < prevAnyButton.length; id++) {
            boolean now = btn(id);
            if (captureCallback != null && now && !prevAnyButton[id]) {
                IntConsumer cb = captureCallback;
                captureCallback = null;
                prevAnyButton[id] = true;
                cb.accept(id);
                continue;
            }
            prevAnyButton[id] = now;
        }
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
        pollCapture();

        GamepadConfig cfg = GamepadConfig.get();
        Options o = mc.options;
        // 26.3では画面の管理がMinecraftからGuiに移動している
        boolean noScreen = mc.gui.screen() == null;
        boolean gameActive = mc.player != null && noScreen;
        active = gameActive;

        boolean a = btn(cfg.buttonJumpOrClose);
        boolean b = btn(cfg.buttonPrimary);
        boolean x = btn(cfg.buttonSwapOffhand);
        boolean y = btn(cfg.buttonInventory);
        boolean lb = btn(cfg.buttonPrev);
        boolean rb = btn(cfg.buttonNext);
        boolean rt = axis(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER) > TRIGGER_THRESHOLD;
        boolean lt = axis(SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER) > TRIGGER_THRESHOLD;

        if (gameActive) {
            lastMenuRef = null;

            jumpHeld = a;
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

            if (!noScreen) {
                handleContainerScreen(mc.gui.screen(), b, prevB, lb, prevLb, rb, prevRb);
                // Aで画面を閉じる(ボタン再割り当て中は誤爆しないよう抑止)
                if (a && !prevA && !isCapturing()) mc.gui.setScreen(null);
            } else {
                lastMenuRef = null;
            }
        }

        prevA = a;
        prevRt = rt;
        prevLt = lt;
        prevB = b;
        prevX = x;
        prevY = y;
        prevLb = lb;
        prevRb = rb;
    }

    // ---- 画面(インベントリ等)内のスロット選択・決定 ----
    private static void handleContainerScreen(Screen screen, boolean b, boolean prevB, boolean lb, boolean prevLb, boolean rb, boolean prevRb) {
        if (!(screen instanceof AbstractContainerScreen<?> cs)) {
            lastMenuRef = null;
            return;
        }
        AbstractContainerMenu menu = cs.getMenu();
        List<Slot> slots = menu.slots;
        if (menu != lastMenuRef) {
            lastMenuRef = menu;
            selectedSlotIndex = 0;
        }
        if (slots.isEmpty()) return;
        if (selectedSlotIndex >= slots.size()) selectedSlotIndex = 0;

        if (lb && !prevLb) selectedSlotIndex = Math.floorMod(selectedSlotIndex - 1, slots.size());
        if (rb && !prevRb) selectedSlotIndex = Math.floorMod(selectedSlotIndex + 1, slots.size());

        if (b && !prevB) {
            Slot slot = slots.get(selectedSlotIndex);
            ((ContainerScreenInvoker) cs).gamepadlite$slotClicked(slot, slot.index, 0, ContainerInput.PICKUP);
        }
    }

    /** ContainerScreenRenderMixinから毎フレーム呼ばれる。選択中スロットの枠を描く */
    public static void renderSlotHighlight(AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics) {
        if (pad == 0L) return;
        AbstractContainerMenu menu = screen.getMenu();
        if (menu != lastMenuRef) return;
        List<Slot> slots = menu.slots;
        if (slots.isEmpty() || selectedSlotIndex < 0 || selectedSlotIndex >= slots.size()) return;

        Slot slot = slots.get(selectedSlotIndex);
        ContainerScreenAccessor acc = (ContainerScreenAccessor) screen;
        int x = acc.gamepadlite$getLeftPos() + slot.x;
        int y = acc.gamepadlite$getTopPos() + slot.y;
        int x0 = x - 1, y0 = y - 1, x1 = x + 17, y1 = y + 17;

        graphics.fill(x0, y0, x1, y0 + 2, SLOT_HIGHLIGHT_COLOR);
        graphics.fill(x0, y1 - 2, x1, y1, SLOT_HIGHLIGHT_COLOR);
        graphics.fill(x0, y0, x0 + 2, y1, SLOT_HIGHLIGHT_COLOR);
        graphics.fill(x1 - 2, y0, x1, y1, SLOT_HIGHLIGHT_COLOR);
    }

    private static void resetState(Minecraft mc) {
        active = false;
        moveX = moveY = lookX = lookY = 0f;
        jumpHeld = sneakHeld = sprintHeld = false;
        if (mc.options != null) {
            hold(mc.options.keyAttack, false, prevRt);
            hold(mc.options.keyUse, false, prevLt);
        }
        prevA = prevRt = prevLt = prevB = prevX = prevY = prevLb = prevRb = false;
        lastMenuRef = null;
        selectedSlotIndex = 0;
        captureCallback = null;
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