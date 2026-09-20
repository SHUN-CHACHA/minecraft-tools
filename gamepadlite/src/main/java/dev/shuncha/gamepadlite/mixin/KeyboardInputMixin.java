package dev.shuncha.gamepadlite.mixin;

import dev.shuncha.gamepadlite.GamepadManager;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void gamepadlite$applyGamepad(CallbackInfo ci) {
        if (!GamepadManager.isActive()) return;

        float mx = GamepadManager.moveX();
        float my = GamepadManager.moveY();
        boolean jump = GamepadManager.jumpHeld();
        boolean sneak = GamepadManager.sneakHeld();
        boolean sprint = GamepadManager.sprintHeld();
        boolean stick = mx != 0f || my != 0f;
        if (!stick && !jump && !sneak && !sprint) return;

        Input k = this.keyPresses;
        this.keyPresses = new Input(
                k.forward() || my < -0.3f,
                k.backward() || my > 0.3f,
                k.left() || mx < -0.3f,
                k.right() || mx > 0.3f,
                k.jump() || jump,
                k.shift() || sneak,
                k.sprint() || sprint);

        if (stick) {
            // 左が正(leftImpulse)、前が正(forwardImpulse)。スティックは左=-1、上=-1
            this.moveVector = new Vec2(-mx, -my);
        }
    }
}