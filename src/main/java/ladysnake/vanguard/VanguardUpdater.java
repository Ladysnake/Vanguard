package ladysnake.vanguard;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VanguardUpdater {
    public static void addCustomUpdater(String modid, String updateUrl, Class mainModClass) {
        Vanguard.logger.info("Vanguard is watching " + modid + " for updates");

        // verify it's not a dev environment
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            String minecraftVersion = MinecraftClient.getInstance().getGame().getVersion().getName();
            String modVersion = FabricLoader.getInstance().getModContainer(modid).get().getMetadata().getVersion().getFriendlyString();
            CompletableFuture.supplyAsync(() -> {
                try (Reader reader = new InputStreamReader(new URL(updateUrl + minecraftVersion).openStream())) {
                    JsonParser jp = new JsonParser();
                    JsonElement jsonElement = jp.parse(reader);
                    return jsonElement.getAsJsonObject();
                } catch (MalformedURLException e) {
                    Vanguard.logger.log(Level.ERROR, "Could not get update information because of malformed URL: " + e.getMessage());
                } catch (IOException e) {
                    Vanguard.logger.log(Level.ERROR, "Could not get update information because of I/O Error: " + e.getMessage());
                }

                return null;
            }).thenAcceptAsync(latestVersionJson -> {
                if (latestVersionJson != null) {
                    String latestVersion = latestVersionJson.get("version").getAsString();
                    String latestFileName = latestVersionJson.get("filename").getAsString() + ".future";
                    // if not the latest version, update toast
                    if (!latestVersion.equalsIgnoreCase(modVersion)) {
                        Vanguard.logger.log(Level.INFO, "Currently present version of " + modid + " is " + modVersion + " while the latest version for Minecraft " + minecraftVersion + " is " + latestVersion + "; downloading update");

                        try {
                            // download new jar
                            URL website = new URL(latestVersionJson.get("download").getAsString());
                            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                            FileOutputStream fos = new FileOutputStream("mods/" + latestFileName);
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                            // once done, extract the uninstaller
                            InputStream in = Vanguard.class.getResourceAsStream("/" + Vanguard.UNINSTALLER);
                            Files.copy(in, Paths.get("mods/" + Vanguard.UNINSTALLER), StandardCopyOption.REPLACE_EXISTING);

                            // get the old version file name
                            String oldFile = new File(mainModClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                try {
                                    Vanguard.logger.log(Level.INFO, "Minecraft instance shutting down, uninstalling " + oldFile);
                                    new ProcessBuilder("java", "-jar", "mods/" + Vanguard.UNINSTALLER, "mods/" + oldFile, "mods/" + latestFileName).start();
                                } catch (IOException e) {
                                    Vanguard.logger.log(Level.ERROR, "Could not run uninstaller");
                                    e.printStackTrace();
                                }
                            }));
                            Vanguard.UPDATED_MODS.add(modid);
                        } catch (MalformedURLException e) {
                            Vanguard.logger.log(Level.ERROR, "Could not download update because of malformed URL: " + e.getMessage());
                        } catch (IOException e) {
                            Vanguard.logger.log(Level.ERROR, "Could not download update because of I/O Error: " + e.getMessage());
                        }
                    }
                } else {
                    Vanguard.logger.log(Level.WARN, "Update information could not be retrieved, auto-update will not be available");
                }
            }, MinecraftClient.getInstance());
        }
    }

    public static void addCurseProxyUpdater(String modid, String cfProjectId, Class mainModClass) {
        Vanguard.UPDATED_MODS.add(modid);
        Vanguard.logger.info("Vanguard is watching " + modid + " for updates");

        // verify it's not a dev environment
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            String minecraftVersion = MinecraftClient.getInstance().getGame().getVersion().getName();
            String modVersion = FabricLoader.getInstance().getModContainer(modid).get().getMetadata().getVersion().getFriendlyString();
            CompletableFuture.supplyAsync(() -> {
                try (Reader reader = new InputStreamReader(new URL("https://curse.nikky.moe/api/addon/" + cfProjectId + "/files").openStream())) {
                    JsonParser jp = new JsonParser();
                    JsonElement jsonElement = jp.parse(reader);

//                    // check if we actually get a file
//                    if (latest.length > 0) {
//                        return res.status(200).json({
//                                version: /(\d+\.\d+(\.\d)*)/g.exec(latest[0].fileName)[1],
//                                filename: latest[0].fileName,
//                                download: latest[0].downloadUrl
//                    });
//                    } else {
//                        return res.status(404);
//                    }
//                    }

                    JsonObject latestFile = null;
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                    Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();
                    while (iterator.hasNext()) {
                        JsonObject fileJson = iterator.next().getAsJsonObject();

                        if (fileJson.get("gameVersion").getAsJsonArray().contains(new JsonPrimitive(minecraftVersion))) {
                            try {
                                if (latestFile == null || sdf.parse(fileJson.get("fileDate").getAsString()).after(sdf.parse(latestFile.get("fileDate").getAsString()))) {
                                    latestFile = fileJson.getAsJsonObject();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (latestFile != null) {
                        JsonObject ret = new JsonObject();

                        Pattern pattern = Pattern.compile("-(\\d+\\.\\d+(\\.\\d)*)");
                        Matcher matcher = pattern.matcher(latestFile.get("fileName").getAsString());
                        matcher.find();
                        ret.add("version", new JsonPrimitive(matcher.group(1)));
                        ret.add("filename", latestFile.get("fileName"));
                        ret.add("download", latestFile.get("downloadUrl"));

                        return ret;
                    }
                } catch (MalformedURLException e) {
                    Vanguard.logger.log(Level.ERROR, "Could not get update information because of malformed URL: " + e.getMessage());
                } catch (IOException e) {
                    Vanguard.logger.log(Level.ERROR, "Could not get update information because of I/O Error: " + e.getMessage());
                }

                return null;
            }).thenAcceptAsync(latestVersionJson -> {
                if (latestVersionJson != null) {
                    String latestVersion = latestVersionJson.get("version").getAsString();
                    String latestFileName = latestVersionJson.get("filename").getAsString() + ".future";
                    // if not the latest version, update toast
                    if (!latestVersion.equalsIgnoreCase(modVersion)) {
                        Vanguard.logger.log(Level.INFO, "Currently present version of " + modid + " is " + modVersion + " while the latest version for Minecraft " + minecraftVersion + " is " + latestVersion + "; downloading update");

                        try {
                            // download new jar
                            URL website = new URL(latestVersionJson.get("download").getAsString());
                            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                            FileOutputStream fos = new FileOutputStream("mods/" + latestFileName);
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                            // once done, extract the uninstaller
                            InputStream in = Vanguard.class.getResourceAsStream("/" + Vanguard.UNINSTALLER);
                            Files.copy(in, Paths.get("mods/" + Vanguard.UNINSTALLER), StandardCopyOption.REPLACE_EXISTING);

                            // get the old version file name
                            String oldFile = new File(mainModClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                try {
                                    Vanguard.logger.log(Level.INFO, "Minecraft instance shutting down, uninstalling " + oldFile);
                                    new ProcessBuilder("java", "-jar", "mods/" + Vanguard.UNINSTALLER, "mods/" + oldFile, "mods/" + latestFileName).start();
                                } catch (IOException e) {
                                    Vanguard.logger.log(Level.ERROR, "Could not run uninstaller");
                                    e.printStackTrace();
                                }
                            }));
                            Vanguard.UPDATED_MODS.add(modid);
                        } catch (MalformedURLException e) {
                            Vanguard.logger.log(Level.ERROR, "Could not download update because of malformed URL: " + e.getMessage());
                        } catch (IOException e) {
                            Vanguard.logger.log(Level.ERROR, "Could not download update because of I/O Error: " + e.getMessage());
                        }
                    }
                } else {
                    Vanguard.logger.log(Level.WARN, "Update information could not be retrieved, auto-update will not be available");
                }
            }, MinecraftClient.getInstance());
        }
    }

}
