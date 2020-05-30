package com.dbsoftwares.spigot.scoreboard.board;

import com.dbsoftwares.configuration.api.IConfiguration;
import com.dbsoftwares.spigot.scoreboard.HeroicScoreboard;
import com.dbsoftwares.spigot.scoreboard.config.ScoreboardConfiguration;
import com.dbsoftwares.spigot.scoreboard.config.ScoreboardLine;
import com.dbsoftwares.spigot.scoreboard.packetwrappers.WrapperPlayServerScoreboardDisplayObjective;
import com.dbsoftwares.spigot.scoreboard.packetwrappers.WrapperPlayServerScoreboardObjective;
import com.dbsoftwares.spigot.scoreboard.packetwrappers.WrapperPlayServerScoreboardScore;
import com.dbsoftwares.spigot.scoreboard.packetwrappers.WrapperPlayServerScoreboardTeam;
import com.dbsoftwares.spigot.scoreboard.utils.PacketUtils;
import com.dbsoftwares.spigot.scoreboard.utils.ServerVersion;
import com.dbsoftwares.spigot.scoreboard.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.*;

public class Scoreboard
{

    private static final ScheduledExecutorService SCHEDULER;
    public static ExecutorService THREAD_POOL;

    static
    {
        SCHEDULER = Executors.newSingleThreadScheduledExecutor();

        final IConfiguration config = HeroicScoreboard.getInstance().getConfiguration();
        if ( config.getBoolean( "async.enabled" ) && config.getBoolean( "async.thread-pool.enabled" ) )
        {
            THREAD_POOL = Executors.newFixedThreadPool( config.getInteger( "async.thread-pool.size" ) );
        }
        else
        {
            THREAD_POOL = null;
        }
    }

    private final ScoreboardConfiguration configuration;
    private final Player player;
    private final String scoreboardName;
    private final ExecutorService executorService;
    private WrapperPlayServerScoreboardObjective objective;
    private WrapperPlayServerScoreboardDisplayObjective display;
    private VirtualTeam[] lines = new VirtualTeam[15];
    private boolean created;
    private ScheduledFuture<?> task;

    public Scoreboard( final Player player, final ScoreboardConfiguration scoreboardConfig )
    {
        this.player = player;
        this.configuration = scoreboardConfig.copy();
        this.scoreboardName = Utils.optionalSubString( "HSB#" + player.getName(), 16 );
        this.executorService = configuration.getInterval() > 0
                ? (THREAD_POOL == null ? Executors.newSingleThreadExecutor() : THREAD_POOL)
                : null;
    }

    public Player getPlayer()
    {
        return player;
    }

    private String getNextTitle()
    {
        return configuration.getTitle().next( this.player );
    }

    public void create()
    {
        if ( created )
        {
            return;
        }
        this.objective = PacketUtils.createScoreboardObjectivePacket(
                this.scoreboardName,
                getNextTitle(),
                "integer",
                (byte) 0
        );
        this.display = PacketUtils.createScoreboardDisplayPacket(
                (byte) 1,
                this.scoreboardName
        );

        this.objective.sendPacket( player );
        this.display.sendPacket( player );

        for ( int i = 0; i < configuration.getLines().size(); i++ )
        {
            this.setLine( i, configuration.getLines().get( i ).next( this.player ) );
        }

        HeroicScoreboard.getInstance().getScoreboards().add( this );

        if ( configuration.getInterval() > 0 )
        {
            task = SCHEDULER.scheduleAtFixedRate(
                    () ->
                    {
                        if ( HeroicScoreboard.getInstance().getConfiguration().getBoolean( "async.enabled" ) )
                        {
                            executorService.execute( this::update );
                        }
                        else
                        {
                            Bukkit.getScheduler().runTask( HeroicScoreboard.getInstance(), this::update );
                        }
                    },
                    configuration.getInterval(),
                    configuration.getInterval(),
                    TimeUnit.MILLISECONDS
            );
        }
        created = true;
    }

    public void update()
    {
        if ( !created )
        {
            return;
        }
        if ( player == null || !player.isOnline() )
        {
            destroy();
            return;
        }
        if ( configuration.getTitle().canRun() )
        {
            this.objective = PacketUtils.createScoreboardObjectivePacket(
                    this.scoreboardName,
                    getNextTitle(),
                    "integer",
                    (byte) 2
            );

            this.objective.sendPacket( player );
        }
        else
        {
            configuration.getTitle().reduceStayTime();
        }

        for ( int i = 0; i < configuration.getLines().size(); i++ )
        {
            final ScoreboardLine line = configuration.getLines().get( i );

            if ( line.canRun() )
            {
                this.setLine( i, line.next( this.player ) );
            }
            else
            {
                line.reduceStayTime();
            }
        }
    }

    public void destroy()
    {
        if ( !created )
        {
            return;
        }
        if ( task != null )
        {
            task.cancel( true );
        }

        if ( THREAD_POOL == null && executorService != null )
        {
            executorService.shutdown();
        }

        final WrapperPlayServerScoreboardObjective objective = PacketUtils.createScoreboardObjectivePacket(
                this.scoreboardName, null, null, (byte) 1
        );

        objective.sendPacket( player );

        for ( VirtualTeam team : lines )
        {
            if ( team != null )
            {
                team.removeTeam().sendPacket( player );
            }
        }

        HeroicScoreboard.getInstance().getScoreboards().remove( this );

        created = false;
    }

    private void removeLine( int line )
    {
        final VirtualTeam team = getOrCreateTeam( line );
        final String old = team.getCurrentPlayer();

        if ( old != null && created )
        {
            removeLine( old ).sendPacket( player );
            team.removeTeam().sendPacket( player );
        }

        lines[line] = null;
    }

    private WrapperPlayServerScoreboardScore removeLine( final String old )
    {
        return PacketUtils.createScoreboardScorePacket( old, (byte) 1, this.scoreboardName, 0 );
    }

    private void setLine( int line, String value )
    {
        final VirtualTeam team = getOrCreateTeam( line );
        final String old = team.getCurrentPlayer();

        team.setValue( value );

        if ( old != null && created && team.playerChanged && configuration.getMode() == ScoreboardMode.SCOREBOARD_MAX_48 )
        {
            removeLine( old ).sendPacket( player );
        }

        sendLine( line );
    }

    private void sendLine( int line )
    {
        if ( line < 0 || line > 14 )
        {
            return;
        }
        final int score = (15 - line);
        final VirtualTeam val = getOrCreateTeam( line );

        for ( WrapperPlayServerScoreboardTeam packet : val.sendLine() )
        {
            packet.sendPacket( player );
        }
        sendScore( val.getCurrentPlayer(), score ).sendPacket( player );
        val.reset();
    }

    private VirtualTeam getOrCreateTeam( int line )
    {
        if ( lines[line] == null )
        {
            lines[line] = new VirtualTeam( Utils.requiresOldScoreboard( player ), configuration.getMode(), line, ServerVersion.search(), "__fakeScore" + line );
        }

        return lines[line];
    }

    private WrapperPlayServerScoreboardScore sendScore( String line, int score )
    {
        return PacketUtils.createScoreboardScorePacket( line, (byte) 0, this.scoreboardName, score );
    }
}