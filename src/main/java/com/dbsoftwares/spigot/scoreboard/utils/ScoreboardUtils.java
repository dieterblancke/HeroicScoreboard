package com.dbsoftwares.spigot.scoreboard.utils;

import com.dbsoftwares.spigot.scoreboard.HeroicScoreboard;
import com.dbsoftwares.spigot.scoreboard.board.ScoreboardMode;
import com.dbsoftwares.spigot.scoreboard.config.ScoreboardConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ScoreboardUtils
{

    public static ScoreboardConfiguration getScoreboardConfigurationFor( final Player player )
    {
        return HeroicScoreboard.getInstance().getScoreboardConfigurations()
                .stream()
                .filter( config -> config.getPermission().isEmpty() || player.hasPermission( config.getPermission() ) )
                .filter( config -> config.checkScript( player ) )
                .min( ( o1, o2 ) -> Integer.compare( o2.getWeight(), o1.getWeight() ) )
                .orElse( null );
    }


    public static String[] splitString( final boolean oldScoreboard, final ScoreboardMode mode, final String str, final ServerVersion version )
    {
        final String[] splitten = new String[3];
        splitten[0] = "";
        splitten[1] = "";
        splitten[2] = "";

        if ( version.isNewerThan( ServerVersion.MINECRAFT_1_13 ) && !oldScoreboard )
        {
            if ( str.length() < 16 )
            {
                splitten[0] = str;
            }
            else
            {
                final int middle = str.length() / 2;

                final String prefix = str.substring( 0, middle );
                final String lastColors = ChatColor.getLastColors( prefix );

                splitten[0] = prefix;
                splitten[2] = lastColors + str.substring( middle );
            }
            return splitten;
        }

        if ( str.length() < 16 )
        {
            splitten[0] = str;
            return splitten;
        }

        if ( mode == ScoreboardMode.SCOREBOARD_MAX_48 )
        {
            return splitLongLine( str );
        }
        else
        {
            return splitShortLine( str );
        }
    }

    // TODO: maybe merge splitShortLine & splitLongLine into one method and remove a bunch of redundant code (to other methods?)

    private static String[] splitShortLine( final String str )
    {
        final String[] splitten = new String[3];

        String prefix = str.substring( 0, 16 );
        String suffix = str.substring( 16 );
        String lastColors = ChatColor.getLastColors( prefix );

        if ( prefix.endsWith( String.valueOf( ChatColor.COLOR_CHAR ) ) )
        {
            prefix = prefix.substring( 0, 15 );
            suffix = ChatColor.COLOR_CHAR + suffix;
        }
        if ( !lastColors.isEmpty() )
        {
            // str ends with color char but there is no follow up, so we have to move it to the next str.
            suffix = lastColors + suffix;
        }

        if ( suffix.length() > 16 )
        {
            suffix = suffix.substring( 0, 16 );
        }

        splitten[0] = prefix;
        splitten[1] = "";
        splitten[2] = suffix;
        return splitten;
    }

    private static String[] splitLongLine( final String str )
    {
        final String[] splitten = new String[3];

        String prefix = str.substring( 0, 16 );
        String player = str.substring( 16 );
        String lastColors = ChatColor.getLastColors( prefix );

        if ( prefix.endsWith( String.valueOf( ChatColor.COLOR_CHAR ) ) )
        {
            prefix = prefix.substring( 0, 15 );
            player = ChatColor.COLOR_CHAR + player;
        }
        if ( !lastColors.isEmpty() )
        {
            // str ends with color char but there is no follow up, so we have to move it to the next str.
            player = lastColors + player;
        }

        if ( player.length() <= 16 )
        {
            splitten[0] = prefix;
            splitten[1] = player;
            return splitten;
        }
        else
        {
            String suffix = player.substring( 16 );
            player = player.substring( 0, 16 );
            lastColors = ChatColor.getLastColors( player );

            if ( player.endsWith( String.valueOf( ChatColor.COLOR_CHAR ) ) )
            {
                player = player.substring( 0, 15 );
                suffix = ChatColor.COLOR_CHAR + suffix;
            }
            if ( !lastColors.isEmpty() )
            {
                // str ends with color char but there is no follow up, so we have to move it to the next str.
                suffix = lastColors + suffix;
            }

            if ( suffix.length() > 16 )
            {
                suffix = suffix.substring( 0, 16 );
            }

            splitten[0] = prefix;
            splitten[1] = player;
            splitten[2] = suffix;
            return splitten;
        }
    }
}
