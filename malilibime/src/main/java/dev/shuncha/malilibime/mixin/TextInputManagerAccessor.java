package dev.shuncha.malilibime.mixin;

import com.mojang.blaze3d.platform.TextInputManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextInputManager.class)
public interface TextInputManagerAccessor {
    @Accessor("textInputEnabled")
    boolean malilibime$isTextInputEnabled();
}