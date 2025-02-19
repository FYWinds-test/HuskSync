package net.william278.husksync.data;

import net.william278.husksync.config.Settings;
import net.william278.husksync.logger.DummyLogger;
import net.william278.husksync.player.DummyPlayer;
import net.william278.husksync.player.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for the data system {@link DataAdapter}
 */
public class DataAdaptionTests {

    @Test
    public void testJsonDataAdapter() {
        final OnlineUser dummyUser = DummyPlayer.create();
        final AtomicBoolean isEquals = new AtomicBoolean(false);
        dummyUser.getUserData(new DummyLogger(), DummySettings.get()).join().ifPresent(dummyUserData -> {
            final DataAdapter dataAdapter = new JsonDataAdapter();
            final byte[] data = dataAdapter.toBytes(dummyUserData);
            final UserData deserializedUserData = dataAdapter.fromBytes(data);

            isEquals.set(deserializedUserData.getInventoryData().serializedItems
                                 .equals(dummyUserData.getInventoryData().serializedItems)
                         && deserializedUserData.getEnderChestData().serializedItems
                                 .equals(dummyUserData.getEnderChestData().serializedItems)
                         && deserializedUserData.getPotionEffectsData().serializedPotionEffects
                                 .equals(dummyUserData.getPotionEffectsData().serializedPotionEffects)
                         && deserializedUserData.getStatusData().health == dummyUserData.getStatusData().health
                         && deserializedUserData.getStatusData().hunger == dummyUserData.getStatusData().hunger
                         && deserializedUserData.getStatusData().saturation == dummyUserData.getStatusData().saturation
                         && deserializedUserData.getStatusData().saturationExhaustion == dummyUserData.getStatusData().saturationExhaustion
                         && deserializedUserData.getStatusData().selectedItemSlot == dummyUserData.getStatusData().selectedItemSlot
                         && deserializedUserData.getStatusData().totalExperience == dummyUserData.getStatusData().totalExperience
                         && deserializedUserData.getStatusData().maxHealth == dummyUserData.getStatusData().maxHealth
                         && deserializedUserData.getStatusData().healthScale == dummyUserData.getStatusData().healthScale);
        });
        Assertions.assertTrue(isEquals.get());
    }

    @Test
    public void testJsonFormat() {
        final OnlineUser dummyUser = DummyPlayer.create();
        final String expectedJson = "{\"status\":{\"health\":20.0,\"max_health\":20.0,\"health_scale\":0.0,\"hunger\":20,\"saturation\":5.0,\"saturation_exhaustion\":5.0,\"selected_item_slot\":1,\"total_experience\":100,\"experience_level\":1,\"experience_progress\":1.0,\"game_mode\":\"SURVIVAL\",\"is_flying\":false},\"inventory\":{\"serialized_items\":\"\"},\"ender_chest\":{\"serialized_items\":\"\"},\"potion_effects\":{\"serialized_potion_effects\":\"\"},\"advancements\":[],\"statistics\":{\"untyped_statistics\":{},\"block_statistics\":{},\"item_statistics\":{},\"entity_statistics\":{}},\"location\":{\"world_name\":\"dummy_world\",\"world_uuid\":\"00000000-0000-0000-0000-000000000000\",\"world_environment\":\"NORMAL\",\"x\":0.0,\"y\":64.0,\"z\":0.0,\"yaw\":90.0,\"pitch\":180.0},\"persistent_data_container\":{\"persistent_data_map\":{}},\"minecraft_version\":\"1.19-beta123456\",\"format_version\":2}";
        AtomicReference<String> json = new AtomicReference<>();
        dummyUser.getUserData(new DummyLogger(), DummySettings.get()).join().ifPresent(dummyUserData -> {
            final DataAdapter dataAdapter = new JsonDataAdapter();
            final byte[] data = dataAdapter.toBytes(dummyUserData);
            json.set(new String(data, StandardCharsets.UTF_8));
        });
        Assertions.assertEquals(expectedJson, json.get());
    }

    @Test
    public void testCompressedDataAdapter() {
        final OnlineUser dummyUser = DummyPlayer.create();
        AtomicBoolean isEquals = new AtomicBoolean(false);
        dummyUser.getUserData(new DummyLogger(), DummySettings.get()).join().ifPresent(dummyUserData -> {
            final DataAdapter dataAdapter = new CompressedDataAdapter();
            final byte[] data = dataAdapter.toBytes(dummyUserData);
            final UserData deserializedUserData = dataAdapter.fromBytes(data);

            isEquals.set(deserializedUserData.getInventoryData().serializedItems
                                 .equals(dummyUserData.getInventoryData().serializedItems)
                         && deserializedUserData.getEnderChestData().serializedItems
                                 .equals(dummyUserData.getEnderChestData().serializedItems)
                         && deserializedUserData.getPotionEffectsData().serializedPotionEffects
                                 .equals(dummyUserData.getPotionEffectsData().serializedPotionEffects)
                         && deserializedUserData.getStatusData().health == dummyUserData.getStatusData().health
                         && deserializedUserData.getStatusData().hunger == dummyUserData.getStatusData().hunger
                         && deserializedUserData.getStatusData().saturation == dummyUserData.getStatusData().saturation
                         && deserializedUserData.getStatusData().saturationExhaustion == dummyUserData.getStatusData().saturationExhaustion
                         && deserializedUserData.getStatusData().selectedItemSlot == dummyUserData.getStatusData().selectedItemSlot
                         && deserializedUserData.getStatusData().totalExperience == dummyUserData.getStatusData().totalExperience
                         && deserializedUserData.getStatusData().maxHealth == dummyUserData.getStatusData().maxHealth
                         && deserializedUserData.getStatusData().healthScale == dummyUserData.getStatusData().healthScale);
        });
        Assertions.assertTrue(isEquals.get());
    }

    private String getTestSerializedPersistentDataContainer() {
        final HashMap<String, PersistentDataTag<?>> persistentDataTest = new HashMap<>();
        persistentDataTest.put("husksync:byte_test", new PersistentDataTag<>(BukkitPersistentDataTagType.BYTE, 0x01));
        persistentDataTest.put("husksync:double_test", new PersistentDataTag<>(BukkitPersistentDataTagType.DOUBLE, 2d));
        persistentDataTest.put("husksync:string_test", new PersistentDataTag<>(BukkitPersistentDataTagType.STRING, "test"));
        persistentDataTest.put("husksync:int_test", new PersistentDataTag<>(BukkitPersistentDataTagType.INTEGER, 3));
        persistentDataTest.put("husksync:long_test", new PersistentDataTag<>(BukkitPersistentDataTagType.LONG, 4L));
        persistentDataTest.put("husksync:float_test", new PersistentDataTag<>(BukkitPersistentDataTagType.FLOAT, 5f));
        persistentDataTest.put("husksync:short_test", new PersistentDataTag<>(BukkitPersistentDataTagType.SHORT, 6));
        final PersistentDataContainerData persistentDataContainerData = new PersistentDataContainerData(persistentDataTest);

        final DataAdapter dataAdapter = new JsonDataAdapter();
        UserData userData = new UserData();
        userData.persistentDataContainerData = persistentDataContainerData;
        return dataAdapter.toJson(userData, false);
    }

    @Test
    public void testPersistentDataContainerSerialization() {
        Assertions.assertEquals(getTestSerializedPersistentDataContainer(), "{\"persistent_data_container\":{\"persistent_data_map\":{\"husksync:int_test\":{\"type\":\"INTEGER\",\"value\":3},\"husksync:string_test\":{\"type\":\"STRING\",\"value\":\"test\"},\"husksync:long_test\":{\"type\":\"LONG\",\"value\":4},\"husksync:byte_test\":{\"type\":\"BYTE\",\"value\":1},\"husksync:short_test\":{\"type\":\"SHORT\",\"value\":6},\"husksync:double_test\":{\"type\":\"DOUBLE\",\"value\":2.0},\"husksync:float_test\":{\"type\":\"FLOAT\",\"value\":5.0}}},\"format_version\":0}");
    }

    // For testing settings
    private static class DummySettings extends Settings {
        private DummySettings(@NotNull Map<ConfigOption, Object> settings) {
            super(settings);
        }

        public static DummySettings get() {
            return new DummySettings(Map.of(
                    ConfigOption.SYNCHRONIZATION_SAVE_DEAD_PLAYER_INVENTORIES, true
            ));
        }
    }


}
