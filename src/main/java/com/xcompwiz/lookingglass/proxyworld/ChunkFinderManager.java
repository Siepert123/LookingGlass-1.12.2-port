package com.xcompwiz.lookingglass.proxyworld;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Ken Butler/shadowking97
 */
public class ChunkFinderManager {
    public static final ChunkFinderManager instance = new ChunkFinderManager();

    private final List<ChunkFinder> finders;

    public ChunkFinderManager() {
        this.finders = new LinkedList<>();
    }

    public void addFinder(ChunkFinder finder) {
        this.finders.add(finder);
    }

    public void tick() {
        for (int i = 0; i < this.finders.size(); i++) {
            if (this.finders.get(i).findChunks()) {
                this.finders.remove(i);
                --i;
            }
        }
    }
}
