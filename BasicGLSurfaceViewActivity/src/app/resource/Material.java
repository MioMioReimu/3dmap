package app.resource;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Material {
	public String name;
	public String texturepath;
	private Bitmap texture;
	public int textureID;
	public Material(String name){
		
	}
	public Material(Element e){
		this.name=e.getTagName();
		NodeList ndList=e.getChildNodes();
		for(int i=0;i<ndList.getLength();i++)
		{
			Element nd=(Element)ndList.item(i);
			if(nd.getTagName().equals("diffusemap")){
				this.texturepath=nd.getAttribute("path");
			}
		}
		this.texture = BitmapFactory.decodeFile(this.texturepath);
	}
	public boolean InitHardwareBuffer(){
		int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        this.textureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
		return true;
	}
	public void UseMaterial(){
		assert(textureID!=0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
	}
}
