package dev.shuncha.gamepadlite.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("clickCount")
    int gamepadlite$getClickCount();

    @Accessor("clickCount")
    void gamepadlite$setClickCount(int value);
}