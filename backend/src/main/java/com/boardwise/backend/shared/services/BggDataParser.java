package com.boardwise.backend.shared.services;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.boardwise.backend.shared.model.BggStats;
import com.boardwise.backend.shared.model.Boardgame;

// reads the <statistics> block off a bgg <item>.
// bgg only sends the block when stats=1 is on the request, and leaves out individual
// fields for games nobody has rated, so everything here is optional.
// shared by the ingest and the refresh job so the two can't drift apart
public final class BggDataParser {

    private BggDataParser() {
    }

    public static BggStats parseStats(Element item) {
        if (item.getElementsByTagName("statistics").getLength() == 0)
            return null;

        return BggStats.builder()
                .owned(intAttr(item, "owned"))
                .usersRated(intAttr(item, "usersrated"))
                .average(doubleAttr(item, "average"))
                .bayesAverage(doubleAttr(item, "bayesaverage"))
                .wishing(intAttr(item, "wishing"))
                .wanting(intAttr(item, "wanting"))
                .trading(intAttr(item, "trading"))
                .numComments(intAttr(item, "numcomments"))
                .build();
    }

    public static Boardgame parseGame(Element item){
        NodeList gameGenres = item.getElementsByTagName("link");
        List<String> genres = new ArrayList<>();
        for(int j = 0; j < gameGenres.getLength(); j++){
            Node genreNode = gameGenres.item(j);
            Node type = genreNode.getAttributes()
                            .getNamedItem("type");
            
            if(type != null && type.getNodeValue().equals("boardgamecategory")){
                String genre = genreNode.getAttributes().getNamedItem("value").getNodeValue();
                genres.add(genre.toLowerCase());
            }
        }

        // API game data object
        return Boardgame.builder()
                .id(null)
                .bggId(null)
                .title(rawAttr(item, "name"))
                .description(tagTextContent(item, "description"))
                .imageURL(tagTextContent(item, "image"))
                .minPlayers(intAttr(item, "minplayers"))
                .maxPlayers(intAttr(item, "maxplayers"))
                .minAge(intAttr(item, "minage"))
                .duration(intAttr(item, "playingtime"))
                .genres(genres)
                .build();
    }

    public static Integer parseYearPublished(Element item) {
        return intAttr(item, "yearpublished");
    }

    private static String tagTextContent(Element item, String tag){
        NodeList nodes = item.getElementsByTagName(tag);
        if(nodes.getLength() == 0)
            return null;

        return nodes.item(0).getTextContent();
    }

    private static String rawAttr(Element item, String tag) {
        NodeList nodes = item.getElementsByTagName(tag);
        if (nodes.getLength() == 0)
            return null;

        Node valueNode = nodes.item(0).getAttributes().getNamedItem("value");
        return valueNode != null ? valueNode.getNodeValue() : null;
    }

    private static Integer intAttr(Element item, String tag) {
        String raw = rawAttr(item, tag);
        try {
            return raw != null ? Integer.valueOf(raw) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double doubleAttr(Element item, String tag) {
        String raw = rawAttr(item, tag);
        try {
            return raw != null ? Double.valueOf(raw) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}