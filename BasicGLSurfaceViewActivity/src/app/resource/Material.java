package app.resource;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Material {
	public String name;
	public HashMap<String, Texture> textures;
	public Texture[] texturelist;
	public String shadername;
	public Shader shader;

	public Material(String name) {

	}

	public Material(Element e) {
		this.name = e.getTagName();
		this.textures = new HashMap<String, Texture>();
		NodeList ndList = e.getElementsByTagName("texture");
		texturelist=new Texture[ndList.getLength()];
		for (int i = 0; i < ndList.getLength(); i++) {
			Element nd = (Element) ndList.item(i);
			Texture t = new Texture();
			t.name = nd.getAttribute("name");
			t.path = nd.getAttribute("path");
			String type = nd.getAttribute("type");
			if (type.equals("diffuse")) {
				t.type = Texture.DIFFUSE;
			}
			t.loadTexture();
			texturelist[i]=t;
			textures.put("t.name", t);
		}
	}
	public void setShader()
	{
		
	}
}
