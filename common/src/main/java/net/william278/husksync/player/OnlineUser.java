package net.william278.husksync.player;

import de.themoep.minedown.MineDown;
import net.william278.husksync.config.Settings;
import net.william278.husksync.data.*;
import net.william278.husksync.editor.ItemEditorMenu;
import net.william278.husksync.event.EventCannon;
import net.william278.husksync.event.PreSyncEvent;
import net.william278.husksync.util.Logger;
import net.william278.desertwell.Version;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Represents a logged-in {@link User}
 */
public abstract class OnlineUser extends User {

    public OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }

    /**
     * Get the player's {@link StatusData}
     *
     * @return the player's {@link StatusData}
     */
    public abstract CompletableFuture<StatusData> getStatus();

    /**
     * Set the player's {@link StatusData}
     *
     * @param statusData      the player's {@link StatusData}
     * @param statusDataFlags the flags to use for setting the status data
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setStatus(@NotNull StatusData statusData,
                                                      @NotNull List<StatusDataFlag> statusDataFlags);

    /**
     * Get the player's inventory {@link ItemData} contents
     *
     * @return The player's inventory {@link ItemData} contents
     */
    public abstract CompletableFuture<ItemData> getInventory();

    /**
     * Set the player's {@link ItemData}
     *
     * @param itemData The player's {@link ItemData}
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setInventory(@NotNull ItemData itemData);

    /**
     * Get the player's ender chest {@link ItemData} contents
     *
     * @return The player's ender chest {@link ItemData} contents
     */
    public abstract CompletableFuture<ItemData> getEnderChest();

    /**
     * Set the player's {@link ItemData}
     *
     * @param enderChestData The player's {@link ItemData}
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setEnderChest(@NotNull ItemData enderChestData);


    /**
     * Get the player's {@link PotionEffectData}
     *
     * @return The player's {@link PotionEffectData}
     */
    public abstract CompletableFuture<PotionEffectData> getPotionEffects();

    /**
     * Set the player's {@link PotionEffectData}
     *
     * @param potionEffectData The player's {@link PotionEffectData}
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setPotionEffects(@NotNull PotionEffectData potionEffectData);

    /**
     * Get the player's set of {@link AdvancementData}
     *
     * @return the player's set of {@link AdvancementData}
     */
    public abstract CompletableFuture<List<AdvancementData>> getAdvancements();

    /**
     * Set the player's {@link AdvancementData}
     *
     * @param advancementData List of the player's {@link AdvancementData}
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setAdvancements(@NotNull List<AdvancementData> advancementData);

    /**
     * Get the player's {@link StatisticsData}
     *
     * @return The player's {@link StatisticsData}
     */
    public abstract CompletableFuture<StatisticsData> getStatistics();

    /**
     * Set the player's {@link StatisticsData}
     *
     * @param statisticsData The player's {@link StatisticsData}
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setStatistics(@NotNull StatisticsData statisticsData);

    /**
     * Get the player's {@link LocationData}
     *
     * @return the player's {@link LocationData}
     */
    public abstract CompletableFuture<LocationData> getLocation();

    /**
     * Set the player's {@link LocationData}
     *
     * @param locationData the player's {@link LocationData}
     * @return a future returning void when complete
     */
    public abstract CompletableFuture<Void> setLocation(@NotNull LocationData locationData);

    /**
     * Get the player's {@link PersistentDataContainerData}
     *
     * @return The player's {@link PersistentDataContainerData} when fetched
     */
    public abstract CompletableFuture<PersistentDataContainerData> getPersistentDataContainer();

    /**
     * Set the player's {@link PersistentDataContainerData}
     *
     * @param persistentDataContainerData The player's {@link PersistentDataContainerData} to set
     * @return A future returning void when complete
     */
    public abstract CompletableFuture<Void> setPersistentDataContainer(@NotNull PersistentDataContainerData persistentDataContainerData);

    /**
     * Indicates if the player has gone offline
     *
     * @return {@code true} if the player has left the server; {@code false} otherwise
     */
    public abstract boolean isOffline();

    /**
     * Returns the implementing Minecraft server version
     *
     * @return The Minecraft server version
     */
    @NotNull
    public abstract Version getMinecraftVersion();

    /**
     * Set {@link UserData} to a player
     *
     * @param data     The data to set
     * @param settings Plugin settings, for determining what needs setting
     * @return a future returning a boolean when complete; if the sync was successful, the future will return {@code true}
     */
    public final CompletableFuture<Boolean> setData(@NotNull UserData data, @NotNull Settings settings,
                                                    @NotNull EventCannon eventCannon, @NotNull Logger logger,
                                                    @NotNull Version serverMinecraftVersion) {
        return CompletableFuture.supplyAsync(() -> {
            // Prevent synchronising user data from newer versions of Minecraft
            if (Version.fromMinecraftVersionString(data.getMinecraftVersion()).compareTo(serverMinecraftVersion) > 0) {
                logger.log(Level.SEVERE, "Cannot set data for " + username +
                                         " because the Minecraft version of their user data (" + data.getMinecraftVersion() +
                                         ") is newer than the server's Minecraft version (" + serverMinecraftVersion + ").");
                return false;
            }
            // Prevent synchronising user data from newer versions of the plugin
            if (data.getFormatVersion() > UserData.CURRENT_FORMAT_VERSION) {
                logger.log(Level.SEVERE, "Cannot set data for " + username +
                                         " because the format version of their user data (v" + data.getFormatVersion() +
                                         ") is newer than the current format version (v" + UserData.CURRENT_FORMAT_VERSION + ").");
                return false;
            }

            // Fire the PreSyncEvent
            final PreSyncEvent preSyncEvent = (PreSyncEvent) eventCannon.firePreSyncEvent(this, data).join();
            final UserData finalData = preSyncEvent.getUserData();
            final List<CompletableFuture<Void>> dataSetOperations = new ArrayList<>() {{
                if (!isOffline() && !preSyncEvent.isCancelled()) {
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_INVENTORIES)) {
                        add(setInventory(finalData.getInventoryData()));
                    }
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_ENDER_CHESTS)) {
                        add(setEnderChest(finalData.getEnderChestData()));
                    }
                    add(setStatus(finalData.getStatusData(), StatusDataFlag.getFromSettings(settings)));
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_POTION_EFFECTS)) {
                        add(setPotionEffects(finalData.getPotionEffectsData()));
                    }
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_ADVANCEMENTS)) {
                        add(setAdvancements(finalData.getAdvancementData()));
                    }
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_STATISTICS)) {
                        add(setStatistics(finalData.getStatisticsData()));
                    }
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_LOCATION)) {
                        add(setLocation(finalData.getLocationData()));
                    }
                    if (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SYNC_PERSISTENT_DATA_CONTAINER)) {
                        add(setPersistentDataContainer(finalData.getPersistentDataContainerData()));
                    }
                }
            }};
            // Apply operations in parallel, join when complete
            return CompletableFuture.allOf(dataSetOperations.toArray(new CompletableFuture[0])).thenApply(unused -> true)
                    .exceptionally(exception -> {
                        // Handle synchronisation exceptions
                        logger.log(Level.SEVERE, "Failed to set data for player " + username + " (" + exception.getMessage() + ")");
                        exception.printStackTrace();
                        return false;
                    }).join();
        });

    }

    /**
     * Dispatch a MineDown-formatted message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public abstract void sendMessage(@NotNull MineDown mineDown);

    /**
     * Dispatch a MineDown-formatted action bar message to this player
     *
     * @param mineDown the parsed {@link MineDown} to send
     */
    public abstract void sendActionBar(@NotNull MineDown mineDown);

    /**
     * Returns if the player has the permission node
     *
     * @param node The permission node string
     * @return {@code true} if the player has permission node; {@code false} otherwise
     */
    public abstract boolean hasPermission(@NotNull String node);

    /**
     * Show the player a {@link ItemEditorMenu} GUI
     *
     * @param menu The {@link ItemEditorMenu} interface to show
     */
    public abstract void showMenu(@NotNull ItemEditorMenu menu);

    /**
     * Returns true if the player is dead
     *
     * @return true if the player is dead
     */
    public abstract boolean isDead();

    /**
     * Get the player's current {@link UserData} in an {@link Optional}
     * <p>
     * If the {@code SYNCHRONIZATION_SAVE_DEAD_PLAYER_INVENTORIES} ConfigOption has been set,
     * the user's inventory will only be returned if they are alive
     * <p>
     * If the user data could not be returned due to an exception, the optional will return empty
     *
     * @param logger The logger to use for handling exceptions
     * @return the player's current {@link UserData} in an optional; empty if an exception occurs
     */
    public final CompletableFuture<Optional<UserData>> getUserData(@NotNull Logger logger, @NotNull Settings settings) {
        return CompletableFuture.supplyAsync(() -> Optional.of(new UserData(getStatus().join(),
                        (settings.getBooleanValue(Settings.ConfigOption.SYNCHRONIZATION_SAVE_DEAD_PLAYER_INVENTORIES)
                                ? getInventory().join() : (isDead() ? new ItemData("") : getInventory().join())),
                        getEnderChest().join(), getPotionEffects().join(), getAdvancements().join(),
                        getStatistics().join(), getLocation().join(), getPersistentDataContainer().join(),
                        getMinecraftVersion().toString())))
                .exceptionally(exception -> {
                    logger.log(Level.SEVERE, "Failed to get user data from online player " + username + " (" + exception.getMessage() + ")");
                    exception.printStackTrace();
                    return Optional.empty();
                });
    }

}
