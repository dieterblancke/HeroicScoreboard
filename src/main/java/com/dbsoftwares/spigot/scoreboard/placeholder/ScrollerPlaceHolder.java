package com.dbsoftwares.spigot.scoreboard.placeholder;

import com.dbsoftwares.spigot.scoreboard.config.ScoreboardIterable;
import com.dbsoftwares.spigot.scoreboard.utils.ServerVersion;
import com.dbsoftwares.spigot.scoreboard.utils.StringReference;
import com.dbsoftwares.spigot.scoreboard.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrollerPlaceHolder extends PlaceHolder
{

    @Override
    public boolean detect( final StringReference reference )
    {
        final String text = reference.getText();

        if ( !text.contains( "scroller" ) )
        {
            return false;
        }

        final Document document = reference.getDocument();
        final Elements elements = document.select( "scroller" );

        return elements.size() >= 1;
    }

    @Override
    public void replace( final Player player, final StringReference reference, final ScoreboardIterable iterable )
    {
        String message = reference.getText();
        final Document document = reference.getDocument();
        final Elements elements = document.select( "scroller" );

        for ( Element element : elements )
        {
            final String text = element.text();
            final int maxWidth = parseInt( element, "max-width", 24, ServerVersion.search().isNewerThan( ServerVersion.MINECRAFT_1_13 ) ? -1 : 40 );
            final int spaces = parseInt( element, "spaces", 10 );

            if ( !iterable.hasData( "SCROLLER_" + text ) )
            {
                iterable.setData( "SCROLLER_" + text, new ScrollingString( text, maxWidth, spaces ) );
            }
            final ScrollingString scroller = iterable.getData( "SCROLLER_" + text );

            message = message.replace( element.outerHtml(), scroller.getScrolled() );
            scroller.scrollForward();
        }

        reference.setText( message );
    }

    private int parseInt( final Element element, final String attr, final int def, final int max )
    {
        final int parsed = parseInt( element, attr, def );

        if ( max < 0 )
        {
            return parsed;
        }
        return Math.min( parsed, max );
    }

    private int parseInt( final Element element, final String attr, final int def )
    {
        if ( element.hasAttr( attr ) )
        {
            try
            {
                return Integer.parseInt( element.attr( attr ) );
            }
            catch ( NumberFormatException e )
            {
                return def;
            }
        }
        return def;
    }

    private static class ScrollingString
    {

        private String original;
        private int width;
        private int position;
        private String lastColors = "";
        private int lastColorPos = 0;

        public ScrollingString( final String original, final int width, final int spaces )
        {
            this.original = original + Utils.multiplyCharacters( ' ', spaces );
            if ( width <= 0 )
            {
                throw new IllegalArgumentException( "Width value has to be greater than 0" );
            }
            else if ( this.original.length() < width )
            {
                throw new IllegalArgumentException( "String length has to be greater than the width value" );
            }
            this.width = width;
        }

        public void scrollForward()
        {
            position++;
            if ( position == original.length() )
            {
                reset();
            }
        }

        public void scrollBackward()
        {
            position--;
            if ( position < 0 )
            {
                position = original.length() - 1;
            }
        }

        public void reset()
        {
            position = 0;
            lastColorPos = 0;
        }

        public void append( String s )
        {
            original += s;
        }

        public String getOriginal()
        {
            return this.original;
        }

        public void setOriginal( String original )
        {
            if ( original.length() < width )
            {
                throw new IllegalArgumentException( "String length has to be greater than the width value" );
            }
            this.original = original;
            reset();
        }

        private String checkForChatColor( final String str, final int pos )
        {
            final StringBuilder result = new StringBuilder();

            if ( pos >= str.length() )
            {
                return result.toString();
            }

            if ( str.charAt( pos ) == ChatColor.COLOR_CHAR )
            {
                if ( pos + 1 >= str.length() )
                {
                    return result.toString();
                }
                final char colorChar = str.charAt( pos + 1 );

                lastColorPos = pos + 1;

                result.append( ChatColor.getByChar( colorChar ).toString() );
                result.append( checkForChatColor( str, pos + 2 ) );
            }
            return result.toString();
        }

        public String getScrolled()
        {
            if ( lastColorPos <= position )
            {
                final String colors = checkForChatColor( original, position );

                if ( !colors.isEmpty() && !this.lastColors.equals( colors ) )
                {
                    this.lastColors = colors;
                }
            }

            int width = this.width;
            if ( !this.lastColors.isEmpty() )
            {
                width -= this.lastColors.length();
            }

            final int e = position + width;

            String result;

            if ( e > original.length() )
            {
                String extra = original.substring( 0, width - (original.length() - position) );

                if ( extra.charAt( extra.length() - 1 ) == ChatColor.COLOR_CHAR )
                {
                    extra = extra.substring( 0, extra.length() - 1 );
                }

                result = original.substring( position ) + extra;
            }
            else
            {
                result = original.substring( position, e );
            }

            if ( result.charAt( 0 ) == ChatColor.COLOR_CHAR )
            {
                // if result starts with color char, we'll increment position to make sure next time we don't see the '4' of the '&4' color
                position++;
            }
            return this.lastColors + result;
        }

        public int getWidth()
        {
            return this.width;
        }

        public int getPosition()
        {
            return this.position;
        }
    }
}
