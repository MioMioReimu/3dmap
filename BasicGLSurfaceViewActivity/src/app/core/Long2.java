package app.core;
/**
 * Class for exposing the native Renderscript long2 type back to the Android system.
 **/
public class Long2 {
    public Long2() {
    }

    public Long2(long initX, long initY) {
        x = initX;
        y = initY;
    }

    public long x;
    public long y;
}




