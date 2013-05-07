package app.core;
/**
 * Class for exposing the native Renderscript long3 type back to the Android system.
 **/
public class Long3 {
    public Long3() {
    }

    public Long3(long initX, long initY, long initZ) {
        x = initX;
        y = initY;
        z = initZ;
    }

    public long x;
    public long y;
    public long z;
}




