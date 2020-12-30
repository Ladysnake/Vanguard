package ladysnake.vanguard;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Vanguard implements ModInitializer {
    static final String MODID = "vanguard";
    static final Logger logger = LogManager.getLogger("Vanguard");

    static final String UNINSTALLER = "vanguard-uninstaller.jar";

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
    }
}
