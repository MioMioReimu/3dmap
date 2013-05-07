package app.core;

public class Float3 {
    public Float3() {
    }
    public Float3(float initX, float initY, float initZ) {
        x = initX;
        y = initY;
        z = initZ;
    }
    public void normalize()
    {
    	float len =(float)java.lang.Math.sqrt(x*x+y*y+z*z);
    	if(len==0.0f){
    		y=1.0f;
    	}
        if (1.0f != len) {
            float recipLen = 1.0f / len;
            x *= recipLen;
            y *= recipLen;
            z *= recipLen;
        }
    }
    public float x;
    public float y;
    public float z;
}
