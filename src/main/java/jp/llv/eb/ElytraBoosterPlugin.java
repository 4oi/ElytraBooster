/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author toyblocks
 */
public class ElytraBoosterPlugin extends JavaPlugin implements Listener {

    private double velocity = 2;
    private boolean consume = true;
    private Material booster = Material.FEATHER;
    private double gravityCoefficient = 4;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.velocity = this.getConfig().getDouble("velocity", this.velocity);
        this.consume = this.getConfig().getBoolean("consume", this.consume);
        this.gravityCoefficient = this.getConfig().getDouble("gravity-coefficient", this.gravityCoefficient);
        try {
            this.booster = Material.getMaterial(this.getConfig().getString("booster").toUpperCase());
        } catch (IllegalArgumentException ex) {
        }
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
                .multiply(this.velocity)
                .divide(new Vector(1, this.gravityCoefficient, 1))
                .add(p.getVelocity());
        if (v.lengthSquared() > 100) {
            v = v.normalize().multiply(10);
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
