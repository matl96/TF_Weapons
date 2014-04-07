package de.TF.TF_Weapons;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.Matl.MatlsTool.*;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.google.common.primitives.Bytes;
import com.mewin.WGRegionEvents.events.RegionEnteredEvent;
import com.mewin.WGRegionEvents.events.RegionEvent;
import com.mewin.WGRegionEvents.events.RegionLeftEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class MTListener implements Listener 
{
	private MTFlags plugin;
	
	public MTListener(MTFlags plugin) 
	{
		this.plugin = plugin;
	}
	
	private WorldGuardPlugin getWorldGuard() 
	{
	    Plugin wg = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (wg == null || !(wg instanceof WorldGuardPlugin)) {
	    	plugin.getLogger().warning("WorldGuard ist nicht installiert!!!");
	        return null;
	    }
	 
	    return (WorldGuardPlugin) wg;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@EventHandler
	public void onRegionLeft(RegionLeftEvent e) 
	{
		if (e.getRegion().getFlag(MTFlags.PLAYER_CMD_EXIT_FLAG) != null) 
			parseCmd(e.getPlayer(), (Set)e.getRegion().getFlag(MTFlags.PLAYER_CMD_EXIT_FLAG), e);
		
		if (e.getRegion().getFlag(MTFlags.SERVER_CMD_EXIT_FLAG) != null) 
			parseCmd(this.plugin.getServer().getConsoleSender(), (Set)e.getRegion().getFlag(MTFlags.SERVER_CMD_EXIT_FLAG), e);
		
		if(e.getRegion().getFlag(MTFlags.PERM_CMD_EXIT_FLAG) != null) {
			Set<String> flagString = e.getRegion().getFlag(MTFlags.PERM_CMD_EXIT_FLAG);
			Player player = e.getPlayer();
			for (String permToken : flagString) {
				String perm = permToken.split(" ")[0];
				String[] doCMD = permToken.replace(perm, "").split("\\|");
				if(player.hasPermission(perm)) {
					permCMD(this.plugin.getServer().getConsoleSender(), doCMD[0].trim(), e);
				} else {
					if(doCMD.length > 1)
						permCMD(this.plugin.getServer().getConsoleSender(), doCMD[1].trim(), e);
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@EventHandler
	public void onRegionEntered(RegionEnteredEvent e) 
	{
		if (e.getRegion().getFlag(MTFlags.PLAYER_CMD_ENTRY_FLAG) != null)
			parseCmd(e.getPlayer(), (Set)e.getRegion().getFlag(MTFlags.PLAYER_CMD_ENTRY_FLAG), e);
		
		if (e.getRegion().getFlag(MTFlags.SERVER_CMD_ENTRY_FLAG) != null) 
			parseCmd(this.plugin.getServer().getConsoleSender(), (Set)e.getRegion().getFlag(MTFlags.SERVER_CMD_ENTRY_FLAG), e);
		
		if (e.getRegion().getFlag(MTFlags.PERM_CMD_ENTRY_FLAG) != null) {
			Set<String> flagString = e.getRegion().getFlag(MTFlags.PERM_CMD_ENTRY_FLAG);
			Player player = e.getPlayer();
			for (String permToken : flagString) {
				String perm = permToken.split(" ")[0];
				String[] doCMD = permToken.replace(perm, "").split("\\|");
				if(player.hasPermission(perm)) {
					permCMD(this.plugin.getServer().getConsoleSender(), doCMD[0].trim(), e);
				} else {
					if(doCMD.length > 1)
						permCMD(this.plugin.getServer().getConsoleSender(), doCMD[1].trim(), e);
				}
			}
		}
	}
	
	/**
	 * Checks if a {@code Player} (or his {@code Arrow}) can damage the creatures of another {@code Player}: 
	 * If the {@code Player} can't build on the {@code Location} where the {@code LivingEntity} stands,
	 * the incoming {@code Damage} is blocked and the attacking player gets 1/4 Damage of his actual {@code Health}
	 * 
	 * @param e
	 * @author matl96
	 * @since MTFlags 1.1
	 * @deprecated is automatically called by {@code EventHandler}, so you don't need to use it.
	 */
	@EventHandler
	public void onMobDmg(EntityDamageByEntityEvent e) 
	{
		//Checks if the attacker is a Player, or a Arrow shot by a Player
		if((e.getDamager() instanceof Player) || (e.getDamager() instanceof Arrow)) {
			Player player;
			boolean dmgSrcIsArrow = false;
			Arrow pfeil = null;
			if(e.getDamager() instanceof Arrow) {
				pfeil = (Arrow) e.getDamager();
				ProjectileSource evtlPlayer = pfeil.getShooter();
				if(evtlPlayer instanceof Player) {
					player = (Player) evtlPlayer;
					dmgSrcIsArrow = true;
				} else {
					return;
				}
			} else {player = (Player) e.getDamager();}
			//Actual Code
			if(!((player.hasPermission("mt.flags.blockmobdmg.op")  || getWorldGuard().canBuild(player, e.getEntity().getLocation())))) {
				if(e.getEntity() instanceof Animals) {
					double dmg = player.getHealth()/4;
					e.setCancelled(true);
					player.damage(dmg);
					if(dmgSrcIsArrow) {
						pfeil.remove();
						if(!(player.getGameMode() == GameMode.CREATIVE))
								player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
					}
					MessageMatil.sendFormatteldPlayer(player, "Diese Kreatur gehört dir nicht!");
				}
			} else {
				soulEaterEntityDmg(e.getEntity(), player, e);
				golf(e.getEntity(), player, e);
				if(!dmgSrcIsArrow)
					frostMourneEntityDmg(e.getEntity(), player, e);
			}
		}
		
	}
	
	public void frostMourneEntityDmg(Entity mob, Player player, EntityDamageByEntityEvent e) {
		if(mob instanceof LivingEntity) {
			ItemStack item = player.getItemInHand();
			if(Material.getMaterial("DIAMOND_SWORD").equals(item.getType())) {
				String itemName = item.getItemMeta().getDisplayName();
				if(itemName != null)
					if("§bFrostmourne".equals(itemName.trim())) {
						LivingEntity newMob = (LivingEntity) mob;
						if(player.isSneaking()) {
							player.launchProjectile(Arrow.class);
						} else {
							Block baseBlock = newMob.getLocation().getBlock();
							Block scndBlock = baseBlock.getRelative(BlockFace.UP, 1);
							List<Block> blockList = new ArrayList<Block>();
							blockList.add(baseBlock);
							blockList.add(scndBlock);
							blockList.add(baseBlock.getRelative(BlockFace.UP, 2));
							blockList.add(baseBlock.getRelative(BlockFace.DOWN));
							blockList.add(baseBlock.getRelative(BlockFace.EAST));
							blockList.add(baseBlock.getRelative(BlockFace.WEST));
							blockList.add(baseBlock.getRelative(BlockFace.SOUTH));
							blockList.add(baseBlock.getRelative(BlockFace.NORTH));
							blockList.add(scndBlock.getRelative(BlockFace.EAST));
							blockList.add(scndBlock.getRelative(BlockFace.WEST));
							blockList.add(scndBlock.getRelative(BlockFace.SOUTH));
							blockList.add(scndBlock.getRelative(BlockFace.NORTH));
							
							for (Block block : blockList) {
								if(blockIsPlacable(block, false)) 
									block.setType(Material.ICE);
							}
							item.setDurability((short) 0);
						}
					}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void soulEaterEntityDmg(Entity mob, Player player, EntityDamageByEntityEvent e) 
	{
		if(mob instanceof LivingEntity) {
			ItemStack item = player.getItemInHand();
			if(Material.getMaterial("IRON_HOE").equals(item.getType())) {
				String itemName = item.getItemMeta().getDisplayName();
				if(itemName != null)
					if("§4Soul Eater".equals(itemName.trim())) {
						LivingEntity newMob = (LivingEntity) mob;
						//BaseDmg
						double dmg = (((Math.random()*newMob.getHealth())/(player.getHealth()/10))+(newMob.getMaxHealth()/10))*(newMob.getFallDistance()+1);
						if(player.isSprinting() && !player.isOnGround()) {
							newMob.getWorld().playEffect(newMob.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
							newMob.getWorld().playEffect(newMob.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
							newMob.getWorld().playEffect(newMob.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
							newMob.setFireTicks(50);
							newMob.damage(dmg*((player.getFallDistance()/1.9)+1.6));
							knockback(player, newMob, 1.75, 0);
						} else if(player.isSprinting()) {
							newMob.getWorld().playEffect(newMob.getLocation(), Effect.SMOKE, 0);
							newMob.damage(dmg*(1.2));
							knockback(player, newMob, 1.5, 0.4);
						} else if(!player.isOnGround()) {
							newMob.getWorld().playEffect(newMob.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
							newMob.damage(dmg*((player.getFallDistance()/2)+1.5));
							knockback(player, newMob, 0.75, 0);
						} if(player.isSneaking()) {
							e.setCancelled(true);
							knockback(player, newMob, 0.5, 1);
						} else {
							newMob.damage(dmg);
							knockback(player, newMob, 0.5, 0);
						}
					}
			}
		}
	}
	
	public void golf(Entity mob, Player player, EntityDamageByEntityEvent e) 
	{
		if(mob instanceof LivingEntity) {
			ItemStack item = player.getItemInHand();
			if(Material.getMaterial("WOOD_SPADE").equals(item.getType())) {
				String itemName = item.getItemMeta().getDisplayName();
				if(itemName != null)
					if("§6Golfschläger".equals(itemName.trim())) {
						LivingEntity newMob = (LivingEntity) mob;
						knockback(player, newMob, 3, 1.3);
						e.setCancelled(true);
					}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void soulEaterHit(PlayerInteractEvent e) {
		if((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			Player player = e.getPlayer();
			ItemStack item = player.getItemInHand();
			if(Material.getMaterial("IRON_HOE").equals(item.getType())) {
				String itemName = item.getItemMeta().getDisplayName();
				if(itemName != null)
					if("§4Soul Eater".equals(itemName.trim())) {
						if(player.isSneaking()) {
							List<Block> blockList = new ArrayList<Block> (player.getLineOfSight(null, 13));
							try {
								blockList.remove(0);
								blockList.remove(0);
								blockList.remove(0);
							} catch(IndexOutOfBoundsException ex) {
								MessageMatil.sendFormatteldPlayer(player, "Du machst da was Falsch ._. Liegt vlt dadran dass nichts da ist was brennen kann, mh?");
								return;
							}
							List<Block> newList = new ArrayList<Block>(blockList);
							player.getWorld().playEffect(player.getLocation(), Effect.GHAST_SHOOT, 0);
							for (Block block : blockList) {
								newList.add(player.getWorld().getBlockAt(block.getX(), block.getY()-1, block.getZ()));
							}
							for (Block block : newList) {
								if(blockIsPlacable(block, true)) {
									block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
									List<Entity> near = block.getLocation().getWorld().getEntities();
									for (Entity ent : near) {
										if(ent.getLocation().distance(block.getLocation()) <= 1)  {
											ent.setFireTicks(50);
											knockback(player, ent, 3, player.getLocation().distance(ent.getLocation())/5);
										}
									}
									block.setType(Material.FIRE);
								}
							}
							player.setFireTicks(0);
							player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 1));
						}
					}
			}
		}
	}
	
	/**
	 * Pushes the {@code Entity mob} with the given {@code strength} away.
	 * 
	 * @param player
	 * @param mob
	 * @param strength
	 */
    public void knockback(Player player, Entity mob, double strength, double Y)
    {
	    Location loc = mob.getLocation().subtract(player.getLocation());
	    double distance = mob.getLocation().distance(player.getLocation());
	    Vector v = loc.toVector().multiply(strength/distance).setY(Y);
	    mob.setVelocity(v);
    }
    
	private void parseCmd(CommandSender cs, Set<String> cmds, RegionEvent e) 
	{
		for (String cmd : cmds) {
			cmd = cmd.replaceAll("\\{player\\}", e.getPlayer().getName());
			cmd = cmd.replaceAll("\\{region\\}", e.getRegion().getId());
			cmd = cmd.replaceAll("\\{comma\\}", ",");
			if (cmd.startsWith("/")) {
				cmd = cmd.substring(1);
			}
			this.plugin.getServer().dispatchCommand(cs, cmd);
		}
	}
	
	private void permCMD(CommandSender cs, String cmd, RegionEvent e) 
	{
		cmd = cmd.replaceAll("\\{player\\}", e.getPlayer().getName());
		cmd = cmd.replaceAll("\\{region\\}", e.getRegion().getId());
		cmd = cmd.replaceAll("\\{comma\\}", ",");
		if (cmd.startsWith("/")) {
			cmd = cmd.substring(1);
		}
		this.plugin.getServer().dispatchCommand(cs, cmd);
	}
	
	public boolean blockIsPlacable(Block block, boolean isSoulEater) {
		Material type = block.getType();
		if(!isSoulEater) {
			if(!type.isSolid()) {
				return true;
			} else 
				return false;
		} else {
			if(!type.isSolid() || (type == Material.CACTUS) || (type == Material.LEAVES) || (type == Material.LEAVES_2) || (type == Material.ICE) || (type == Material.PACKED_ICE) || (type == Material.WATER)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public static String getFacingDirection(Player player) {
		double rot = (player.getLocation().getYaw() - 90) % 360;
		if (rot < 0) {
			rot += 360.0;
		}
		if (0 <= rot && rot < 22.5) {
			return "N";
		} else if (22.5 <= rot && rot < 67.5) {
			return "NE";
		} else if (67.5 <= rot && rot < 112.5) {
			return "E";
		} else if (112.5 <= rot && rot < 157.5) {
			return "SE";
		} else if (157.5 <= rot && rot < 202.5) {
			return "S";
		} else if (202.5 <= rot && rot < 247.5) {
			return "SW";
		} else if (247.5 <= rot && rot < 292.5) {
			return "W";
		} else if (292.5 <= rot && rot < 337.5) {
			return "NW";
		} else if (337.5 <= rot && rot < 360.0) {
			return "N";
		} else {
			return null;
		}
	}
}