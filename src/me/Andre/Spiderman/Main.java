package me.Andre.Spiderman;

import me.Andre.API.HashMapHelper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener {

    public HashMapHelper hmh = new HashMapHelper();
    public Map<String, Integer> tasks;
    public boolean enable;
    BukkitScheduler scheduler;

    @Override
    public void onEnable(){
        scheduler = getServer().getScheduler();
        enable = false;
        tasks = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable(){
        scheduler.cancelTasks(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("enableSpiderman")){
            getServer().broadcastMessage(ChatColor.GREEN + "[Spiderman] Enabled!");
            enable = true;
        }else if(command.getName().equalsIgnoreCase("disableSpiderman")){
            getServer().broadcastMessage(ChatColor.RED + "[Spiderman] Disabled!");
            enable = false;
        }
        return false;
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event){
        if(!enable) return;

        hmh.registerIfNotIncluded(tasks, event.getPlayer().getUniqueId().toString(), 0);
        if(event.getAction().equals(Action.LEFT_CLICK_AIR)){
            event.setCancelled(false);
            if(tasks.get(event.getPlayer().getUniqueId().toString()) == 0){
                Player p = event.getPlayer();
                Location pLoc = p.getLocation();
                World w = p.getWorld();
                Block tb = p.getTargetBlock(null, 120);

                if(!p.isSneaking()){
                    if(pLoc.distance(tb.getLocation()) < 6){
                        return;
                    }
                }


                if(!tb.getType().equals(Material.AIR)){
                    Vector v = pLoc.getDirection();
                    Arrow arr = w.spawnArrow(pLoc.add(v.clone().normalize().multiply(2)), v, 10, 0);
                    LivingEntity bat = (LivingEntity)p.getWorld().spawnEntity(pLoc, EntityType.BAT);
                    bat.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(10000, 10000));
                    bat.addPotionEffect(PotionEffectType.SLOW.createEffect(10000, 10000));

                    bat.setLeashHolder(arr);



                    tasks.replace(event.getPlayer().getUniqueId().toString(), scheduler.scheduleSyncRepeatingTask(this, () -> {
                        if(arr.isInBlock()){
                            p.teleport(p.getLocation().add(0, .5, 0));
                            p.setVelocity(v.clone().multiply(5));
                            bat.remove();
                            arr.remove();
                            scheduler.cancelTask(tasks.get(event.getPlayer().getUniqueId().toString()));
                            tasks.replace(event.getPlayer().getUniqueId().toString(), 0);
                        }
                        if(arr.getTicksLived() > 40 || arr.isDead()){
                            bat.remove();
                            scheduler.cancelTask(tasks.get(event.getPlayer().getUniqueId().toString()));
                            tasks.replace(event.getPlayer().getUniqueId().toString(), 0);
                        }
                    }, 0L, 5L));
                }
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event){
        if(!enable) return;

        if(event.getEntity() instanceof Player){
            if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
                event.setDamage(event.getDamage() / 10);
            }
        }

    }
}
