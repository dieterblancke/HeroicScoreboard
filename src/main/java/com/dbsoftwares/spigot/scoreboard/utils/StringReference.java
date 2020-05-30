package com.dbsoftwares.spigot.scoreboard.utils;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/*
 * Goal of this class is to reuse a document throughout PlaceHolders
 * This is to save resources (aka there is no need to parse a document in every placeholder that wants it)
 */

public class StringReference
{

    @Getter
    private final String originalText;
    @Getter
    @Setter
    private String text;
    private Document document;

    public StringReference( final String text )
    {
        this.originalText = text;
        this.text = text;
    }

    public Document getDocument()
    {
        if ( document == null )
        {
            document = Jsoup.parse( text );
            document.outputSettings().prettyPrint( false );
        }
        return document;
    }
}
