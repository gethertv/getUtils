package dev.gether.getutils.models.particles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DustOptions {
    private int red;
    private int green;
    private int blue;
    private int size;

    public DustOptions() {}

    public DustOptions(int red, int green, int blue, int size) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.size = size;
    }
}
