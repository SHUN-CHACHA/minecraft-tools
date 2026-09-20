package dev.shuncha.gamepadlite;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class GamepadLiteClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(GamepadManager::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> GamepadManager.shutdown());
    }
}