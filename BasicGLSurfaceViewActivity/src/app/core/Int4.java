package app.core;
/**
 * Class for exposing the native Renderscript int4 type back to the Android system.
 *
 **/
public class Int4 {
    public Int4() {
    }

    public Int4(int initX, int initY, int initZ, int initW) {
        x = initX;
        y = initY;
        z = initZ;
        w = initW;
    }

    public int x;
    public int y;
    public int z;
    public int w;
}



