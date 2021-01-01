package ladysnake.vanguard;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vanguard implements ModInitializer {
    public static final String MODID = "vanguard";
    static final Logger logger = LogManager.getLogger("Vanguard");

    static final String UNINSTALLER = "vanguard-uninstaller.jar";

    static final ArrayList<String> UPDATED_MODS = new ArrayList<>();

    @Override
    public void onInitialize() {
        // delete uninstaller
        if (Files.exists(Paths.get("mods/" + UNINSTALLER))) {
            try {
                Files.delete(Paths.get("mods/" + UNINSTALLER));
            } catch (IOException e) {
                logger.log(Level.WARN, "Could not remove uninstaller because of I/O Error: " + e.getMessage());
            }
        }

        // delete all future files
        Pattern pattern = Pattern.compile("-(\\d+\\.\\d+(\\.\\d)*)");
        for (File mod : new File("mods").listFiles()) {
            Matcher matcher = pattern.matcher(mod.getName());
            if (matcher.find()) {
                mod.delete();
            }
        }
    }

    public static ArrayList<String> getUpdatedMods() {
        return UPDATED_MODS;
    }
}
