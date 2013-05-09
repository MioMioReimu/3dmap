package app.resource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import app.resource.Shader.SamplerType;

public class Texture {
	public String name;
	public int id;
	public String path;
	public SamplerType type;
	public Bitmap bmp;
	public static int DIFFUSE=0;
	public void loadTexture()
	{
		bmp=BitmapFactory.decodeFile(this.path);
	}
	public boolean InitHardwareBuffer(){
		int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        this.id = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
		return true;
	}
}
