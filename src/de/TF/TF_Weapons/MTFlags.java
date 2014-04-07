package de.TF.TF_Weapons;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.WGCustomFlags.flags.CustomSetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MTFlags extends JavaPlugin 
{
	public static final StringFlag CMD_FLAG = new StringFlag("cmd");
	public static final CustomSetFlag PLAYER_CMD_ENTRY_FLAG = new CustomSetFlag("player-entry-cmd", CMD_FLAG);
	public static final CustomSetFlag PLAYER_CMD_EXIT_FLAG = new CustomSetFlag("player-exit-cmd", CMD_FLAG);
	public static final CustomSetFlag SERVER_CMD_ENTRY_FLAG = new CustomSetFlag("server-entry-cmd", CMD_FLAG);
	public static final CustomSetFlag SERVER_CMD_EXIT_FLAG = new CustomSetFlag("server-exit-cmd", CMD_FLAG);
	public static final CustomSetFlag PERM_CMD_ENTRY_FLAG = new CustomSetFlag("perm-entry-cmd", CMD_FLAG);
	public static final CustomSetFlag PERM_CMD_EXIT_FLAG = new CustomSetFlag("perm-exit-cmd", CMD_FLAG);
	public static final CustomSetFlag BLOCK_MOB_DMG = new CustomSetFlag("block-mob-dmg", CMD_FLAG);
	private MTListener listener;
	private WGCustomFlagsPlugin custPlugin;
	
	public void onEnable() 
	{
		Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
		if ((plug == null) || (!(plug instanceof WGCustomFlagsPlugin)) || (!plug.isEnabled())) {
			getLogger().warning("WorldGuardCustomFlags ist nicht installiert!!!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		this.custPlugin = ((WGCustomFlagsPlugin)plug);
		
		this.custPlugin.addCustomFlag(PLAYER_CMD_ENTRY_FLAG);
		this.custPlugin.addCustomFlag(PLAYER_CMD_EXIT_FLAG);
		this.custPlugin.addCustomFlag(SERVER_CMD_ENTRY_FLAG);
		this.custPlugin.addCustomFlag(SERVER_CMD_EXIT_FLAG);
		this.custPlugin.addCustomFlag(PERM_CMD_ENTRY_FLAG);
		this.custPlugin.addCustomFlag(PERM_CMD_EXIT_FLAG);
		this.custPlugin.addCustomFlag(BLOCK_MOB_DMG);
		
		this.listener = new MTListener(this);
		
		getServer().getPluginManager().registerEvents(this.listener, this);
	}
	
	public void onDisable() {}
}