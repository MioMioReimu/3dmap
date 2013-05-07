package app.core;
/**
 * Class for exposing the native Renderscript int3 type back to the Android system.
 *
 **/
public class Int3 {
    public Int3() {
    }

    public Int3(int initX, int initY, int initZ) {
        x = initX;
        y = initY;
        z = initZ;
    }

    public int x;
    public int y;
    public int z;
}




