/* 
 * Copyright (C) 2016 toyblocks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.llv.eb;

import java.util.Optional;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Main plugin class. 
 * @author toyblocks
 */
public class ElytraBoosterPlugin extends JavaPlugin implements Listener {

    private double acceleration = 2;
    private boolean consume = true;
    private Material booster = Material.FEATHER;
    private double gravityCoefficient = 4;
    
    private double maxVelocity = 10;
    private boolean accelarationInverselyProportionalToVelocity = false;
    private double maxVelocityY = 10;
    private double maxVelocityXZ = 10;
    
    private boolean cooldown = true;
    private double cooldownTick = 20D;
    private boolean cooldownProportionalToVelocity = false;
    
    private BoostRequirement boostRequirement = BoostRequirement.ELYTRA;
    private boolean flyOnBoost = true;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.acceleration = this.getConfig().getDouble("acceleration", this.acceleration);
        this.consume = this.getConfig().getBoolean("consume", this.consume);
        this.gravityCoefficient = this.getConfig().getDouble("gravity-coefficient", this.gravityCoefficient);
        try {
            this.booster = Material.getMaterial(this.getConfig().getString("booster").toUpperCase());
        } catch (IllegalArgumentException ex) {
        }
        this.maxVelocity = this.getConfig().getDouble("max-velocity", this.maxVelocity);
        this.accelarationInverselyProportionalToVelocity = this.getConfig().getBoolean("accelaration-inversely-proportional-to-velocity", this.accelarationInverselyProportionalToVelocity);
        this.maxVelocityY = this.getConfig().getDouble("y-max-velocity", this.maxVelocityY);
        this.maxVelocityXZ = this.getConfig().getDouble("xz-max-velocity", this.maxVelocityXZ);
        
        this.cooldown = this.getConfig().getBoolean("cooldown", this.cooldown);
        this.cooldownTick = this.getConfig().getDouble("cooldown-tick", this.cooldownTick);
        this.cooldownProportionalToVelocity = this.getConfig().getBoolean("cooldown-proportional-to-velocity", this.cooldownProportionalToVelocity);
        
        try {
            String e = this.getConfig().getString("required-equipment".toUpperCase().replace('-', '_'));
            this.boostRequirement = e == null ? BoostRequirement.FLYING_WITH_ELYTRA : BoostRequirement.valueOf(e);
        }catch(IllegalArgumentException ex) {
        }
        this.flyOnBoost = this.getConfig().getBoolean("fly-on-boost", this.flyOnBoost);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void on(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!p.hasPermission("elytrabooster.boost")
                || event.getAction() != Action.RIGHT_CLICK_AIR
                || event.getItem() == null
                || event.getItem().getType() != this.booster
                || ItemCooldownUtil.inCooldown(p, this.booster)) {
            return;
        }
        switch (this.boostRequirement) {
            case FLYING_WITH_ELYTRA:
                if (!(flyOnBoost || p.isGliding())) {
                    return;
                }
                //break;l not required
            case ELYTRA:
                if (p.getEquipment().getChestplate() == null || p.getEquipment().getChestplate().getType() != Material.ELYTRA) {
                    return;
                }
                break;
        }
        if (!p.isGliding() && flyOnBoost) {
            p.setGliding(true);
        }
        Vector v = p.getEyeLocation().getDirection()
                .multiply(this.acceleration)
                .divide(new Vector(1, this.gravityCoefficient, 1))
                .add(p.getVelocity());
        if (v.lengthSquared() > Math.pow(this.maxVelocity, 2.0D)) {
            v = v.normalize().multiply(this.maxVelocity);
        }
        if (v.getX() > this.maxVelocityXZ) {
            v.setX(this.maxVelocityXZ);
        }
        if (v.getY() > this.maxVelocityY) {
            v.setY(this.maxVelocityY);
        }
        if (v.getZ() > this.maxVelocityXZ) {
            v.setZ(this.maxVelocityXZ);
        }
        p.setVelocity(v);
        if (consume && p.getGameMode() != GameMode.CREATIVE) {
            if (event.getItem().getAmount() != 1) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            } else {
                p.getInventory().remove(event.getItem());
            }
        }
        if (cooldown && p.getGameMode() != GameMode.CREATIVE) {
            ItemCooldownUtil.setCooldown(p, booster, (int) (cooldownTick * (cooldownProportionalToVelocity ? v.length() : 1)));
        }
    }
    
    private enum BoostRequirement {
        NONE, ELYTRA, FLYING_WITH_ELYTRA;
        static Optional<BoostRequirement> of(String name) {
            try {
                return Optional.of(valueOf(name.toUpperCase().replace('-', '_')));
            }catch(IllegalArgumentException ex) {
                return Optional.empty();
            }
        }
    }

}
