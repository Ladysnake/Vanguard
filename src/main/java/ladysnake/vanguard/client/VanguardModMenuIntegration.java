package ladysnake.vanguard.client;

import io.github.prospector.modmenu.api.ModMenuApi;
import ladysnake.vanguard.Config;
import ladysnake.vanguard.Vanguard;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

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

            // create the config
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(new TranslatableText("title.vanguard.config"));

            // config categories and entries
            ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.vanguard.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder
                    .startBooleanToggle(new TranslatableText("option.vanguard.toggle"), Config.isToggled())
                    .setTooltip(
                            new TranslatableText("option.tooltip.vanguard.toggle"))
                    .setSaveConsumer(Config::toggle)
                    .setDefaultValue(true)
                    .build());

            // build and return the config screen
            return builder.build();
        };
    }
}

