package dev.gether.getutils.models.location;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import java.io.Serializable;

@Setter
@Getter
public class BlockData implements Serializable {


    public String blockData;
    private String worldName;
    private int x;
    private int y;
    private int z;

    public BlockData(Block block) {
        blockData = block.getBlockData().getAsString();
        worldName = block.getWorld().getName();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }
}
