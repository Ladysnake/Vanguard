package ladysnake.vanguard.client;

import ladysnake.vanguard.common.Vanguard;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class VanguardClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Vanguard.initialize(MinecraftClient.getInstance());
    }
}