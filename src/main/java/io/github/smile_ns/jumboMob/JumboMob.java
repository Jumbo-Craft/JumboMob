package io.github.smile_ns.jumboMob;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;import org.bukkit.util.Vector;import java.util.Set;

public final class JumboMob extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        setGiantAttribute(living, Attribute.SCALE, 16.0);
        setGiantAttribute(living, Attribute.MOVEMENT_SPEED, 16.0);
        setGiantAttribute(living, Attribute.JUMP_STRENGTH, 4.5);
        setGiantAttribute(living, Attribute.MAX_HEALTH, 16.0);
        living.setHealth(living.getHealth() * 16.0);
        setGiantAttribute(living, Attribute.FOLLOW_RANGE, 16.0);
    }

    void setGiantAttribute(LivingEntity living, Attribute attrType, double scale) {
        AttributeInstance attr = living.getAttribute(attrType);
        if (attr != null) {
            attr.setBaseValue(attr.getValue() * scale);
        }
    }

    private static final Set<EntityType> UNDEAD_TYPES = Set.of(
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIE_HORSE,
            EntityType.DROWNED,
            EntityType.HUSK,
            EntityType.SKELETON,
            EntityType.SKELETON_HORSE,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.WITHER,
            EntityType.PHANTOM,
            EntityType.ZOGLIN,
            EntityType.ZOMBIFIED_PIGLIN
    );

    @EventHandler
    public void onDamaged(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof LivingEntity living)) return;

        if (living instanceof Mob mob) {
            if (UNDEAD_TYPES.contains(mob.getType()) && isDamagedBySunlight(mob, e.getCause())) {
                e.setDamage(e.getDamage() * 16.0);
            }
        }
    }

    static boolean isDamagedBySunlight(Mob mob, EntityDamageEvent.DamageCause cause) {
        if (cause != EntityDamageEvent.DamageCause.FIRE_TICK) return false;

        World world = mob.getWorld();
        Location loc = mob.getLocation();
        long time = world.getTime();

        if (time >= 12000) return false;
        if (world.hasStorm() || world.isThundering()) return false;
        if (loc.getBlock().getLightFromSky() < 15) return false;
        ItemStack helmet = mob.getEquipment().getHelmet();
        if (helmet != null && helmet.getType() != Material.AIR) return false;
        return !mob.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Mob)) return;
        if (!(e.getProjectile() instanceof Arrow arrow)) return;

        Vector velocity = arrow.getVelocity();
        arrow.setVelocity(velocity.multiply(2.0));
    }
}
