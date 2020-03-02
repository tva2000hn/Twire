package com.perflyst.twire.model;

/**
 * Created by SebastianRask on 03-03-2016.
 */
public class ChatEmote {
    private Emote emote;
    private int[] positions;

    public ChatEmote(Emote emote, int[] positions) {
        this.emote = emote;
        this.positions = positions;
    }

    public Emote getEmote() {
        return emote;
    }

    public int[] getPositions() { return positions; }
}
