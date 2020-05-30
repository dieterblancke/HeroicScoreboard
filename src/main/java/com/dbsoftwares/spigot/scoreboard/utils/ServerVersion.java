package com.dbsoftwares.spigot.scoreboard.utils;

public enum ServerVersion
{
    MINECRAFT_1_15( 12, "v1_15_R1" ),
    MINECRAFT_1_14( 11, "v1_14_R1" ),
    MINECRAFT_1_13_1( 10, "v1_13_R2" ),
    MINECRAFT_1_13( 9, "v1_13_R1" ),
    MINECRAFT_1_12( 8, "v1_12_R1" ),
    MINECRAFT_1_11( 7, "v1_11_R1" ),
    MINECRAFT_1_10( 6, "v1_10_R1" ),
    MINECRAFT_1_9_4( 5, "v1_9_R2" ),
    MINECRAFT_1_9( 4, "v1_9_R1" ),
    MINECRAFT_1_8_4( 3, "1_8_R3" ),
    MINECRAFT_1_8_3( 2, "1_8_R2" ),
    MINECRAFT_1_8( 1, "1_8_R1" ),
    UNKNOWN( 0 );

    private int id;
    private String version;

    ServerVersion( int id )
    {
        this.id = id;
    }

    ServerVersion( int id, String version )
    {
        this.id = id;
        this.version = version;
    }

    public static ServerVersion search()
    {
        for ( ServerVersion version : values() )
        {
            if ( Utils.getVersionName().equalsIgnoreCase( version.getVersion() ) )
            {
                return version;
            }
        }
        return UNKNOWN;
    }

    public int getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version;
    }

    public boolean isNewerThan( ServerVersion version )
    {
        return id >= version.getId();
    }

    public boolean isOlderThan( ServerVersion version )
    {
        return id < version.getId();
    }
}