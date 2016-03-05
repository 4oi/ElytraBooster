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
    private double maxVelocityY = 10;
    private double maxVelocityXZ = 10;

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
        this.maxVelocityY = this.getConfig().getDouble("y-max-velocity", this.maxVelocityY);
        this.maxVelocityXZ = this.getConfig().getDouble("xz-max-velocity", this.maxVelocityXZ);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void on(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!p.hasPermission("elytrabooster.boost")
                || event.getAction() != Action.RIGHT_CLICK_AIR
                || p.getEquipment().getChestplate() == null
                || p.getEquipment().getChestplate().getType() != Material.ELYTRA
                || event.getItem() == null
                || event.getItem().getType() != booster) {
            return;
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
    }

}
