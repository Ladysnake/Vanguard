package ladysnake.vanguard.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vanguard {
    public static final String MODID = "vanguard";
    static final Logger logger = LogManager.getLogger("Vanguard");

    static final String UNINSTALLER = "vanguard-uninstaller.jar";

    static final ArrayList<String> UNINSTALLER_PARAMS = new ArrayList<>();

    static final ArrayList<String> MODS = new ArrayList<>();
    static final ArrayList<String> UPDATED_MODS = new ArrayList<>();

    public static void initialize(Executor executor) {
        // load config
        Config.load();

        // delete uninstaller
        if (Files.exists(Paths.get("mods/"+UNINSTALLER))) {
            try {
                Files.delete(Paths.get("mods/"+UNINSTALLER));
            } catch (IOException e) {
                logger.log(Level.WARN, "Could not remove uninstaller because of I/O Error: " + e.getMessage());
            }
        }

        if (Config.isToggled()) {
            // delete all .future files
            Pattern pattern = Pattern.compile("\\.future$");
            for (File mod : new File("mods").listFiles()) {
                Matcher matcher = pattern.matcher(mod.getName());
                if (matcher.find()) {
                    mod.delete();
                }
            }

            // get all mods that must be watched by vanguard
            FabricLoader loader = FabricLoader.getInstance();
            for (ModContainer mod : loader.getAllMods()) {
                String modId = mod.getMetadata().getId();
                CustomValue vanguardData = mod.getMetadata().getCustomValue("vanguard");
                if (vanguardData != null) {
                    MODS.add(modId);
                    CustomValue.CvObject vanguardObj = vanguardData.getAsObject();

                    if (vanguardObj.containsKey("update-url")) {
                        VanguardUpdater.addCustomUpdater(modId, vanguardObj.get("update-url").getAsString(), executor);
                    } else if (vanguardObj.containsKey("curse-project-id")) {
                        VanguardUpdater.addCurseProxyUpdater(modId, vanguardObj.get("curse-project-id").getAsString(), executor);
                    }
                }
            }

            // extract the uninstaller and add a shutdown hook to uninstall old files and install new ones
            InputStream in = Vanguard.class.getResourceAsStream("/" + Vanguard.UNINSTALLER);
            try {
                Files.copy(in, Paths.get("mods/" + UNINSTALLER), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Vanguard.logger.log(Level.INFO, "Minecraft instance shutting down, starting Vanguard uninstaller");

                    StringBuilder commandParams = new StringBuilder();
                    for (String uninstallerParam : UNINSTALLER_PARAMS) {
                        commandParams.append(" ").append(uninstallerParam);
                    }

                    Runtime.getRuntime().exec("java -jar mods/" + Vanguard.UNINSTALLER + commandParams);
                } catch (IOException e) {
                    Vanguard.logger.log(Level.ERROR, "Could not run uninstaller");
                    e.printStackTrace();
                }
            }));
        }
    }

    public static ArrayList<String> getMods() {
        return MODS;
    }
    public static ArrayList<String> getUpdatedMods() {
        return UPDATED_MODS;
    }
}