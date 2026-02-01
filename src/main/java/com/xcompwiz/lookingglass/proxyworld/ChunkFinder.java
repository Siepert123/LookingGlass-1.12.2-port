package com.xcompwiz.lookingglass.proxyworld;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.network.packet.PacketChunkInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.LinkedList;
import java.util.List;

public class ChunkFinder {
    private final IChunkProvider chunkProvider;
    private final int rootX;
    private final int rootZ;
    private final int range;
    private final int dimension;
    private final EntityPlayer player;
    private ChunkData[][] map;
    private List<BlockPos> positions;
    private final int d;
    private int step;
    private int stepRange;
    private long startTime;

    /**
     * Finds exposed chunks. Chunks must be loaded.
     * @param root The chunk in chunk coordinates.
     * @param dimension The target dimension
     * @param chunkProvider The world server that contains the chunks
     * @param player The player to send the chunks to
     * @param range The radius of the chunkfinder.
     * @return Sorted Chunk Data, by range. Prioritizes closest chunks.
     */
    public ChunkFinder(BlockPos root, int dimension, IChunkProvider chunkProvider, EntityPlayer player, int range) {
        this.chunkProvider = chunkProvider;
        this.range = range;
        this.dimension = dimension;
        this.player = player;
        this.d = (range << 1) + 1;
        this.map = new ChunkData[this.d][this.d];
        this.rootX = root.getX() - range;
        this.rootZ = root.getZ() - range;
        this.stepRange = 16 - root.getY();
        if (root.getY() > this.stepRange) this.stepRange = root.getY();
        this.startTime = System.nanoTime();
        LookingGlass.logger().debug("ChunkFinder scan started at nano time {}", this.startTime);
        for (int i = 0; i < this.d; i++) {
            for (int j = 0; j < this.d; j++) {
                this.map[i][j] = new ChunkData(i + this.rootX, j + this.rootZ);
                int x1 = i - this.range;
                int z1 = j - this.range;
                this.map[i][j].distance = x1 * x1 + z1 * z1;
            }
        }
        this.positions = new LinkedList<>();
        this.positions.add(new BlockPos(this.range, root.getY(), this.range));
        this.step = 0;
        List<BlockPos> positions2 = new LinkedList<>();
        while (this.step - 1 < this.stepRange && !this.positions.isEmpty()) {
            while (!this.positions.isEmpty()) {
                positions2.addAll(scan(this.chunkProvider, this.map, this.positions.get(0), this.range));
                this.positions.remove(0);
            }
            this.step++;
            this.positions.addAll(positions2);
            positions2.clear();
            if (this.step >= this.stepRange) {
                int range2 = this.step - this.stepRange + 1;
                range2 *= range2;
                int range3 = this.step - this.stepRange;
                if (range3 < 0) range3 = 0;
                range3 *= range3;
                int minStep = this.range - (this.step - this.stepRange);
                int maxStep = this.range + (this.step - this.stepRange) + 1;
                if (minStep < 0) minStep = 0;
                if (maxStep > this.d) maxStep = this.d;
                for (int i = minStep; i < maxStep; i++) {
                    for (int j = minStep; j < maxStep; j++) {
                        int dist = this.map[i][j].distance;
                        if (this.map[i][j].doAdd() && dist < range2 && dist >= range3) {
                            ChunkData data = this.map[i][j];
                            Chunk c2 = this.chunkProvider.provideChunk(data.x, data.z);
                            if (!c2.isLoaded()) c2 = loadChunk(this.chunkProvider, data.x, data.z);

                            ServerPacketDispatcher.getInstance().addPacket(this.player, PacketChunkInfo.createPacket(c2, true, data.levels(), dimension));
                        }
                    }
                }
            }
        }
    }

    public boolean findChunks() {
        if (!this.positions.isEmpty()) {
            int tick = 0;
            List<BlockPos> positions2 = new LinkedList<>();
            while (!this.positions.isEmpty() && tick < 15) {
                BlockPos pos = this.positions.get(0);
                positions2.addAll(scan(this.chunkProvider, this.map, pos, this.range));
                this.positions.remove(0);
                ++tick;
            }
            if (!this.positions.isEmpty()) return false;

            this.step++;

            this.positions.addAll(positions2);
            positions2.clear();

            if (this.step >= this.stepRange) {
                int range2 = this.step - this.stepRange + 1;
                range2 *= range2;
                int range3 = this.step - this.stepRange;
                if (range3 < 0) range3 = 0;
                range3 *= range3;
                int minStep = this.range - (this.step - this.stepRange);
                int maxStep = this.range + (this.step - this.stepRange) + 1;
                if (minStep < 0) minStep = 0;
                if (maxStep > this.d) maxStep = this.d;
                for (int i = minStep; i < maxStep; i++) {
                    for (int j = minStep; j < maxStep; j++) {
                        int dist = this.map[i][j].distance;
                        if (this.map[i][j].doAdd() && dist < range2 && dist >= range3) {
                            ChunkData data = this.map[i][j];
                            Chunk c2 = this.chunkProvider.provideChunk(data.x, data.z);
                            if (!c2.isLoaded()) c2 = loadChunk(this.chunkProvider, data.x, data.z);
                            ServerPacketDispatcher.getInstance().addPacket(this.player, PacketChunkInfo.createPacket(c2, true, data.levels(), this.dimension));
                        }
                    }
                }
            }
            return false;
        }

        if (this.step >= this.stepRange) {
            int range2 = this.step - this.stepRange;
            range2 *= range2;
            for (int i = 0; i < d; i++) {
                for (int j = 0; j < d; j++) {
                    int dist = this.map[i][j].distance;
                    if (this.map[i][j].doAdd() && dist >= range2) {
                        ChunkData data = map[i][j];
                        Chunk c2 = this.chunkProvider.provideChunk(data.x, data.z);
                        if (!c2.isLoaded()) c2 = loadChunk(this.chunkProvider, data.x, data.z);
                        ServerPacketDispatcher.getInstance().addPacket(this.player, PacketChunkInfo.createPacket(c2, true, data.levels(), this.dimension));
                    }
                }
            }
        }
        LookingGlass.logger().debug("Scan finished in {}ns", System.nanoTime() - this.startTime);
        return true;
    }

    private static List<BlockPos> scan(IChunkProvider chunkProvider, ChunkData[][] map, BlockPos pos, int range) {
        int rangeSqr = range * range;
        List<BlockPos> positions3 = new LinkedList<>();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        ChunkData data = map[x][z];
        if (data.isAdded(y) || data.distance > rangeSqr) return positions3;
        data.add(y);
        Chunk c = chunkProvider.provideChunk(data.x, data.z);
        if (!c.isLoaded()) {
            c = loadChunk(chunkProvider, data.x, data.z);
        }
        if (c.isEmptyBetween(y << 4, (y << 4) + 15)) {
            data.empty(y);
            if (x < (range << 1) && !(map[x + 1][z].isAdded(y) || map[x + 1][z].distance > rangeSqr || map[x + 1][z].distance < map[x][z].distance)) {
                positions3.add(new BlockPos(x + 1, y, z));
            }
            if (x > 0 && !(map[x - 1][z].isAdded(y) || map[x - 1][z].distance > rangeSqr || map[x - 1][z].distance < map[x][z].distance)) {
                positions3.add(new BlockPos(x - 1, y, z));
            }
            if (y < 15 && !(map[x][z].isAdded(y + 1) || map[x][z].distance > rangeSqr)) {
                positions3.add(new BlockPos(x, y + 1, z));
            }
            if (y > 0 && !(map[x][z].isAdded(y - 1) || map[x][z].distance > rangeSqr)) {
                positions3.add(new BlockPos(x, y - 1, z));
            }
            ;
            if (z < (range << 1) && !(map[x][z + 1].isAdded(y) || map[x][z + 1].distance > rangeSqr || map[x][z + 1].distance < map[x][z].distance)) {
                positions3.add(new BlockPos(x, y, z + 1));
            }
            if (z > 0 && !(map[x][z - 1].isAdded(y) || map[x][z - 1].distance > rangeSqr || map[x][z - 1].distance < map[x][z].distance)) {
                positions3.add(new BlockPos(x, y, z - 1));
            }
        } else {
            boolean ok = false;
            if (z > 0 && !(map[x][z - 1].isAdded(y) || map[x][z - 1].distance > rangeSqr || map[x][z - 1].distance < map[x][z].distance)) {
                for (int i = 0; i < 16 && !ok; i++) {
                    for (int l = 0; l < 16 && !ok; l++) {
                        if (!isBlockNormalCubeDefault(c, l, (y << 4) + i, 0, false)) ok = true;
                    }
                }
                if (ok) {
                    positions3.add(new BlockPos(x, y, z - 1));
                }
                ok = false;
            }
            if (z < (range << 1) && !(map[x][z + 1].isAdded(y) || map[x][z + 1].distance > rangeSqr || map[x][z + 1].distance < map[x][z].distance)) {
                for (int i = 0; i < 16 && !ok; i++) {
                    for (int l = 0; l < 16 && !ok; l++) {
                        if (!isBlockNormalCubeDefault(c, l, (y << 4) + i, 15, false)) ok = true;
                    }
                }
                if (ok) {
                    positions3.add(new BlockPos(x, y, z + 1));
                }
                ok = false;
            }
            if (y > 0 && !(map[x][z].isAdded(y - 1) || map[x][z].distance > rangeSqr)) {
                for (int i = 0; i < 16 && !ok; i++) {
                    for (int l = 0; l < 16 && !ok; l++) {
                        if (!isBlockNormalCubeDefault(c, l, (y << 4), i, false)) ok = true;
                    }
                }
                if (ok) {
                    positions3.add(new BlockPos(x, y - 1, z));
                }
                ok = false;
            }
            if (y < 15 && !(map[x][z].isAdded(y + 1) || map[x][z].distance > rangeSqr)) {
                for (int i = 0; i < 16 && !ok; i++) {
                    for (int l = 0; l < 16 && !ok; l++) {
                        if (!isBlockNormalCubeDefault(c, l, (y << 4) + 15, i, false)) ok = true;
                    }
                }
                if (ok) {
                    positions3.add(new BlockPos(x, y + 1, z));
                }
                ok = false;
            }
            if (x > 0 && !(map[x - 1][z].isAdded(y) || map[x - 1][z].distance > rangeSqr || map[x - 1][z].distance < map[x][z].distance)) {
                for (int i = 0; i < 16 && !ok; i++) {
                    for (int l = 0; l < 16 && !ok; l++) {
                        if (!isBlockNormalCubeDefault(c, 0, (y << 4) + l, i, false)) ok = true;
                    }
                }
                if (ok) {
                    positions3.add(new BlockPos(x - 1, y, z));
                }
                ok = false;
            }
            if (x < (range << 1) && !(map[x + 1][z].isAdded(y) || map[x + 1][z].distance > rangeSqr || map[x + 1][z].distance < map[x][z].distance)) {
                for (int i = 0; i < 16 && !ok; i++) {
                    for (int l = 0; l < 16 && !ok; l++) {
                        if (!isBlockNormalCubeDefault(c, 15, (y << 4) + l, i, false)) ok = true;
                    }
                }
                if (ok) {
                    positions3.add(new BlockPos(x + 1, y, z));
                }
            }
        }
        return positions3;
    }

    public static boolean isBlockNormalCubeDefault(Chunk chunk, int x, int y, int z, boolean fallback) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            if (chunk != null && !chunk.isEmpty()) {
                IBlockState state = chunk.getBlockState(x & 15, y, z & 15);
                return state.isNormalCube();
            }
        }
        return fallback;
    }

    public static Chunk loadChunk(IChunkProvider provider, int x, int z) {
        return provider instanceof ChunkProviderServer ? ((ChunkProviderServer)provider).loadChunk(x, z) : provider instanceof ChunkProviderClient ? ((ChunkProviderClient)provider).loadChunk(x, z) : provider.getLoadedChunk(x, z);
    }

    public class ChunkData implements Comparable<ChunkData> {
        public int x;
        public int z;
        public int added;
        public int empty;
        public int distance;

        public ChunkData(int x, int z) {
            this.x = x;
            this.z = z;
            this.added = 0;
        }

        public boolean isAdded(int level) {
            return (this.added & (1 << level)) != 0;
        }
        public boolean doAdd() {
            return (this.added ^ this.empty) != 0;
        }
        public boolean doAdd(int level) {
            return this.isAdded(level) && !this.isEmpty(level);
        }
        public void add(int level) {
            this.added |= 1 << level;
        }
        public boolean isEmpty(int level) {
            return (this.empty & (1 << level)) == 0;
        }
        public void empty(int level) {
            this.empty |= 1 << level;
        }
        public int levels() {
            return this.added ^ this.empty;
        }

        @Override
        public int compareTo(ChunkData o) {
            return this.distance - o.distance;
        }
    }
}
