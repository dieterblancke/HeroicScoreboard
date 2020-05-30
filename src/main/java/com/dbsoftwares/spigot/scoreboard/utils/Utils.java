package com.dbsoftwares.spigot.scoreboard.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Utils
{

    public static String getVersionName()
    {
        return Bukkit.getServer().getClass().getPackage().getName().substring( 23 );
    }

    public static String format( final Player player, String message )
    {
        if ( Bukkit.getPluginManager().isPluginEnabled( "MVdWPlaceholderAPI" ) )
        {
            message = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders( player, message );
        }
        if ( Bukkit.getPluginManager().isPluginEnabled( "PlaceholderAPI" ) )
        {
            message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders( (OfflinePlayer) player, message );
        }
        if ( Bukkit.getPluginManager().isPluginEnabled( "CentrixCore" ) )
        {
            final com.dbsoftwares.ccoreapi.user.User user = com.dbsoftwares.ccoreapi.CCore.a().getUser( player.getName() );

            message = com.dbsoftwares.ccoreapi.placeholders.PlaceHolderAPI.formatMessage( user, message );
        }
        message = ChatColor.translateAlternateColorCodes( '&', message );

        return message;
    }

    public static String c( final String str )
    {
        return ChatColor.translateAlternateColorCodes( '&', str );
    }
}
