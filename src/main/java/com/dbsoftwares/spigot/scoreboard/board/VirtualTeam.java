package com.dbsoftwares.spigot.scoreboard.board;

import com.dbsoftwares.spigot.scoreboard.packetwrappers.WrapperPlayServerScoreboardTeam;
import com.dbsoftwares.spigot.scoreboard.utils.PacketUtils;
import com.dbsoftwares.spigot.scoreboard.utils.ScoreboardUtils;
import com.dbsoftwares.spigot.scoreboard.utils.ServerVersion;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class VirtualTeam
{

    private static final String[] COLOR_LIST = { "§0§r", "§1§r", "§2§r", "§3§r", "§4§r",
            "§5§r", "§6§r", "§7§r", "§8§r", "§9§r", "§a§r", "§b§r", "§c§r", "§d§r", "§e§r", "§f§r" };

    private final boolean oldScoreboard;
    private final ScoreboardMode mode;
    private final int line;
    private final ServerVersion version;
    private final String name;
    public boolean playerChanged = false;
    private String prefix;
    private String suffix;
    private String currentPlayer;
    private String oldPlayer;
    private boolean prefixChanged;
    private boolean suffixChanged;
    private boolean first = true;

    private VirtualTeam( final boolean oldScoreboard, final ScoreboardMode mode, final int line, final ServerVersion version, final String name, final String prefix, final String suffix )
    {
        this.oldScoreboard = oldScoreboard;
        this.mode = mode;
        this.line = line;
        this.version = version;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public VirtualTeam( final boolean oldScoreboard, final ScoreboardMode mode, final int line, final ServerVersion version, final String name )
    {
        this( oldScoreboard, mode, line, version, name, "", "" );
    }

    public String getName()
    {
        return name;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix( String prefix )
    {
        if ( this.prefix == null || !this.prefix.equals( prefix ) )
        {
            this.prefixChanged = true;
        }
        this.prefix = prefix;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix( String suffix )
    {
        if ( this.suffix == null || !this.suffix.equals( prefix ) )
        {
            this.suffixChanged = true;
        }
        this.suffix = suffix;
    }

    private WrapperPlayServerScoreboardTeam createPacket( int mode )
    {
        boolean newChat = version.isNewerThan( ServerVersion.MINECRAFT_1_13 );
        ChatColor lastColor = ChatColor.WHITE;

        for ( int i = 0; i < prefix.length(); i++ )
        {
            final char c = prefix.charAt( i );

            if ( c == ChatColor.COLOR_CHAR && i + 1 < prefix.length() )
            {
                lastColor = ChatColor.getByChar( prefix.charAt( i + 1 ) );
            }
        }

        return PacketUtils.createTeamPacket(
                name,
                (byte) mode,
                name,
                (byte) 0,
                "never",
                "always",
                lastColor.ordinal(),
                prefix,
                suffix,
                new String[0]
        );
    }

    public WrapperPlayServerScoreboardTeam createTeam()
    {
        return createPacket( 0 );
    }

    public WrapperPlayServerScoreboardTeam updateTeam()
    {
        return createPacket( 2 );
    }

    public WrapperPlayServerScoreboardTeam removeTeam()
    {
        final WrapperPlayServerScoreboardTeam team = PacketUtils.createRemoveTeamPacket( name, (byte) 1 );

        first = true;
        return team;
    }

    public void setPlayer( String name )
    {
        if ( this.currentPlayer == null || !this.currentPlayer.equals( name ) )
        {
            this.playerChanged = true;
        }
        this.oldPlayer = this.currentPlayer;
        this.currentPlayer = name;
    }

    public Iterable<WrapperPlayServerScoreboardTeam> sendLine()
    {
        final List<WrapperPlayServerScoreboardTeam> packets = new ArrayList<>();

        if ( first )
        {
            packets.add( createTeam() );
        }
        else if ( prefixChanged || suffixChanged )
        {
            packets.add( updateTeam() );
        }

        if ( first || playerChanged )
        {
            if ( oldPlayer != null )
            {
                packets.add( addOrRemovePlayer( 4, oldPlayer ) );
            }
            packets.add( changePlayer() );

            if ( first )
            {
                first = false;
            }

            if ( playerChanged )
            {
                playerChanged = false;
            }
        }

        return packets;
    }

    public void reset()
    {
        prefixChanged = false;
        suffixChanged = false;
        playerChanged = false;
        oldPlayer = null;
    }

    public WrapperPlayServerScoreboardTeam changePlayer()
    {
        return addOrRemovePlayer( 3, currentPlayer );
    }

    public WrapperPlayServerScoreboardTeam addOrRemovePlayer( int mode, String playerName )
    {
        return PacketUtils.createTeamPlayersPacket( name, (byte) mode, new String[]{ playerName } );
    }

    public String getCurrentPlayer()
    {
        return currentPlayer;
    }

    public String getValue()
    {
        return getPrefix() + getCurrentPlayer() + getSuffix();
    }

    public void setValue( String value )
    {
        final String[] splitten = ScoreboardUtils.splitString( oldScoreboard, mode, value, version );

        if ( splitten[1].isEmpty() )
        {
            splitten[1] = COLOR_LIST[line];
        }

        if ( splitten[0] == null )
        {
            splitten[0] = "";
        }

        if ( splitten[1] == null )
        {
            splitten[1] = "";
        }

        if ( splitten[2] == null )
        {
            splitten[2] = "";
        }

        setPrefix( splitten[0] );
        setPlayer( splitten[1] );
        setSuffix( splitten[2] );
    }
}
