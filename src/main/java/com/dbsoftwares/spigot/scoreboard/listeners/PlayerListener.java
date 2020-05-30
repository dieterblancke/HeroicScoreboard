package com.dbsoftwares.spigot.scoreboard.listeners;

import com.dbsoftwares.spigot.scoreboard.HeroicScoreboard;
import com.dbsoftwares.spigot.scoreboard.board.Scoreboard;
import com.dbsoftwares.spigot.scoreboard.config.ScoreboardConfiguration;
import com.dbsoftwares.spigot.scoreboard.utils.ScoreboardUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{

    @EventHandler
    public void onJoin( final PlayerJoinEvent event )
    {
        final ScoreboardConfiguration config = ScoreboardUtils.getScoreboardConfigurationFor( event.getPlayer() );

        if ( config == null )
        {
            return;
        }

        final Scoreboard scoreboard = new Scoreboard( event.getPlayer(), config );
        scoreboard.create();
    }

    @EventHandler
    public void onQuit( final PlayerQuitEvent event )
    {
        handleDisconnect( event.getPlayer() );
    }

    @EventHandler
    public void onKick( final PlayerKickEvent event )
    {
        handleDisconnect( event.getPlayer() );
    }

    private void handleDisconnect( final Player player )
    {
        final Scoreboard scoreboard = HeroicScoreboard.getInstance().getScoreboard( player );

        if ( scoreboard != null )
        {
            scoreboard.destroy();
        }
    }
}
