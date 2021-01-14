package ladysnake.vanguard.client;

import io.github.prospector.modmenu.api.ModMenuApi;
import ladysnake.vanguard.common.Config;
import ladysnake.vanguard.common.Vanguard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class VanguardModMenuIntegration implements ModMenuApi {
    @Override
    public String getModId() {
        return Vanguard.MODID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return parent -> {
            // load config
            Config.load();

            return new VanguardConfigScreen(MinecraftClient.getInstance().currentScreen);
        };
    }
}

