package ladysnake.vanguard.common;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    public static final Path PROPERTIES_PATH = FabricLoader.getInstance().getConfigDir().resolve("vanguard.properties");
    private static final Properties config = new Properties();
    private static boolean toggle;

    public static void load() {
        // if vanguard.properties exist, load it
        if (Files.isRegularFile(PROPERTIES_PATH)) {
            // load vanguard.properties
            try {
                config.load(Files.newBufferedReader(PROPERTIES_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { // if no vanguard.properties, load default values
            // define default properties
            toggle(true);
        }

        try {
            toggle = Boolean.parseBoolean(config.getProperty("toggle"));
        } catch (Exception e) {
            toggle(true);
        }
    }

    public static void save() {
        try {
            config.store(Files.newBufferedWriter(Config.PROPERTIES_PATH), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isToggled() {
        return toggle;
    }

    public static void toggle(boolean value) {
        toggle = value;
        config.setProperty("toggle", Boolean.toString(value));
        Config.save();
    }
}
