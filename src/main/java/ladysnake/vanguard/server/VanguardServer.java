package ladysnake.vanguard.server;

import ladysnake.vanguard.common.Vanguard;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class VanguardServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(Vanguard::initialize);
    }
}