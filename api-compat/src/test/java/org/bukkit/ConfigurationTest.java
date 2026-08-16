package org.bukkit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTest {
    @Test
    void storesNestedValuesAndReturnsLiveSections() {
        FileConfiguration configuration = new FileConfiguration();
        configuration.set("server.port", 25565);
        configuration.set("server.enabled", true);

        ConfigurationSection server = configuration.getConfigurationSection("server");

        assertTrue(server != null);
        assertEquals(25565, server.getInt("port"));
        assertTrue(server.getBoolean("enabled"));
        server.set("motd", "BlazingMC");
        assertEquals("BlazingMC", configuration.getString("server.motd"));
        assertTrue(configuration.getKeys(true).contains("server.port"));
    }

    @Test
    void defaultsAreUsedOnlyWhenValuesAreMissing() {
        FileConfiguration configuration = new FileConfiguration();
        configuration.addDefault("limit", 10);
        configuration.set("enabled", false);

        assertEquals(10, configuration.getInt("limit"));
        assertTrue(configuration.contains("limit"));
        assertFalse(configuration.contains("limit", true));
        assertFalse(configuration.getBoolean("enabled", true));
    }

    @Test
    void listReadsAreDefensive() {
        FileConfiguration configuration = new FileConfiguration();
        configuration.set("names", List.of("one", "two"));

        List<?> first = configuration.getList("names");
        List<?> second = configuration.getList("names");

        assertNotSame(first, second);
        assertEquals(List.of("one", "two"), first);
    }
}
