package ladysnake.vanguard.uninstaller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        Logger logger = Logger.getLogger("Vanguard Uninstaller");
        FileHandler fileHandler;

        String runningDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParent()+"/";

        try {
            // This block configure the logger with handler and formatter
            fileHandler = new FileHandler("./vanguard-uninstaller.log");
            SimpleFormatter simple = new SimpleFormatter();
            fileHandler.setFormatter(simple);
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                logger.severe(stackTraceElement.toString());
            }
        }

        if (args.length > 0) {
            for (String arg : args) {
                Matcher renameMatch = Pattern.compile("(.+\\.jar)\\.future$").matcher(arg);
                if (renameMatch.matches()) {
                    // renaming new mod file
                    String renamed = renameMatch.group(1);
                    Path source = new File(runningDir+arg).toPath();
                    Path target = new File(runningDir+renamed).toPath();

                    try {
                        Files.move(source, target);
                        logger.info(renamed + " installed successfully");
                    } catch (IOException e) {
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            logger.severe(stackTraceElement.toString());
                        }
                    }
                } else {
                    // removing old mod file
                    File fileToRemove = new File(runningDir+arg);
                    try {
                        fileToRemove.delete();
                        logger.info(arg + " deleted successfully");
                    } catch (Exception e) {
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            logger.severe(stackTraceElement.toString());
                        }
                    }
                }
            }
        } else {
            logger.severe("No parameters were given, shutting down.");
        }
    }
}
