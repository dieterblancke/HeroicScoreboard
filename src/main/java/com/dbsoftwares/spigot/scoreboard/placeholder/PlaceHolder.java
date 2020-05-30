package com.dbsoftwares.spigot.scoreboard.placeholder;

import com.dbsoftwares.spigot.scoreboard.config.ScoreboardIterable;
import com.dbsoftwares.spigot.scoreboard.utils.StringReference;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class PlaceHolder
{

    private static final List<PlaceHolder> PLACE_HOLDERS = Lists.newArrayList();

    public static void registerPlaceHolder( final PlaceHolder placeHolder )
    {
        PLACE_HOLDERS.add( placeHolder );
    }

    public static String formatPlaceHolders( final Player player, final String message, final ScoreboardIterable iterable )
    {
        final StringReference reference = new StringReference( message );

        for ( PlaceHolder placeHolder : PLACE_HOLDERS )
        {
            if ( placeHolder.detect( reference ) )
            {
                placeHolder.replace( player, reference, iterable );
            }
        }

        return reference.getText();
    }

    public abstract boolean detect( final StringReference text );

    public abstract void replace( final Player player, final StringReference text, final ScoreboardIterable iterable );
}
