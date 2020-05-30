package com.dbsoftwares.spigot.scoreboard.config;

import com.dbsoftwares.spigot.scoreboard.board.ScoreboardMode;
import com.dbsoftwares.spigot.scoreboard.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ScoreboardConfiguration
{

    private final String permission;
    private final String checkScript;
    private final int weight;
    private final ScoreboardMode mode;
    private final int interval;
    private final ScoreboardTitle title;
    private final List<ScoreboardLine> lines;

    public ScoreboardConfiguration copy()
    {
        final List<ScoreboardLine> copiedLines = lines.stream().map( ScoreboardLine::copy ).collect( Collectors.toList() );

        return new ScoreboardConfiguration( permission, checkScript, weight, mode, interval, title.copy(), copiedLines );
    }

    public boolean checkScript( final Player player )
    {
        if ( checkScript == null || checkScript.isEmpty() )
        {
            return true;
        }
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName( "nashorn" );

        engine.put( "player", player );

        try
        {
            final AtomicBoolean result = new AtomicBoolean( true );

            engine.put( "result", result );
            engine.eval( Utils.setPlaceHolders( player, checkScript ) );

            return result.get();
        }
        catch ( ScriptException e )
        {
            e.printStackTrace();
            return true;
        }
    }
}
