package app.core;


/**
 * Class for exposing the native Renderscript long4 type back to the Android system.
 **/
public class Long4 {
    public Long4() {
    }

    public Long4(long initX, long initY, long initZ, long initW) {
        x = initX;
        y = initY;
        z = initZ;
        w = initW;
    }

    public long x;
    public long y;
    public long z;
    public long w;
}



