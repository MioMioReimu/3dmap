package app.resource;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import app.resource.Shader.SamplerType;

public class Material {
	public String name;
	public HashMap<String, Texture> textures;
	public Texture[] texturelist;
	public String shadername;
	public Shader shader;

	public Material(String name) {

	}

	public Material(Element e) {
		this.name = e.getAttribute("name");
		this.textures = new HashMap<String, Texture>();
		
		NodeList ndList = e.getElementsByTagName("texture");
		texturelist=new Texture[ndList.getLength()];
		for (int i = 0; i < ndList.getLength(); i++) {
			Element nd = (Element) ndList.item(i);
			Texture t = new Texture();
			t.name = nd.getAttribute("name");
			t.path = nd.getAttribute("path");
			String type = nd.getAttribute("type");
			if (type.equals("sampler2D")) {
				t.type = SamplerType.sampler2D;
			}
			t.loadTexture();
			texturelist[i]=t;
			textures.put("t.name", t);
		}
		this.shadername=((Element)e.getElementsByTagName("shader").item(0)).getAttribute("name");
	}
	public void setShader(Shader s){
		this.shader=s;
	}
	public void InitHardwareBuffer(){
		for(int i=0;i<this.texturelist.length;i++)
			texturelist[i].InitHardwareBuffer();
	}
	public Texture[] getTextures(){
		return this.texturelist;
	}
}
