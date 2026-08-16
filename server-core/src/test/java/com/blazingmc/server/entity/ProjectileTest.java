package com.blazingmc.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileTest {
    @Test
    void projectileAdvancesAndAppliesGravity() {
        ProjectileManager.Projectile projectile = new ProjectileManager.Projectile(
            10000, ProjectileManager.ProjectileType.ARROW, 1,
            0, 64, 0, 1, 1, 0, true);

        projectile.tick();

        assertEquals(1.0, projectile.getX(), 0.0001);
        assertEquals(65.0, projectile.getY(), 0.0001);
        assertTrue(projectile.getVelocityY() < 1.0);
        assertEquals(1, projectile.getAge());
        assertTrue(projectile.isCritical());
    }

    @Test
    void projectileIdentityIsStable() {
        ProjectileManager.Projectile projectile = new ProjectileManager.Projectile(
            10001, ProjectileManager.ProjectileType.TRIDENT, 1,
            0, 64, 0, 0, 0, 1, false);

        assertEquals(10001, projectile.getEntityId());
        assertNotEquals(null, projectile.getUuid());
    }
}
