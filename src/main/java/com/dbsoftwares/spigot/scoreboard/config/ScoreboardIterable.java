package com.dbsoftwares.spigot.scoreboard.config;

import com.dbsoftwares.spigot.scoreboard.placeholder.PlaceHolder;
import com.dbsoftwares.spigot.scoreboard.utils.Utils;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ScoreboardIterable
{

    private final int interval;
    private final List<String> lines;
    private final Map<String, Object> lineData = Maps.newHashMap();

    private int idx = 0;
    private int stayTime;

    public void reduceStayTime()
    {
        if ( stayTime > 0 )
        {
            stayTime--;
        }
    }

    public boolean canRun()
    {
        return interval > 0 && stayTime <= 0;
    }

    public String next( final Player player )
    {
        if ( lines.isEmpty() )
        {
            return "";
        }
        if ( idx + 1 >= lines.size() )
        {
            idx = 0;
        }
        stayTime = interval;

        return PlaceHolder.formatPlaceHolders( player, Utils.format( player, lines.get( idx++ ) ), this );
    }

    public boolean hasData( final String key )
    {
        return lineData.containsKey( key );
    }

    @SuppressWarnings("unchecked")
    public <T> T getData( final String key )
    {
        return (T) lineData.get( key );
    }

    public void setData( final String key, final Object value )
    {
        lineData.put( key, value );
    }
}
