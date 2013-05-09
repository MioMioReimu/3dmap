package app.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MaterialLib {
	private final static String matlibPath="/sdcard/map/matlib/matlib.xml";
	protected HashMap<String, Material>mats;
	public MaterialLib(){
		mats=new HashMap<String, Material>();
		DocumentBuilderFactory xmlFactory=DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlBuilder;
		try {
			xmlBuilder = xmlFactory.newDocumentBuilder();
			Document doc=xmlBuilder.parse(new File(matlibPath));
			Element root=(Element)doc.getDocumentElement();
			NodeList mNdList=root.getElementsByTagName("material");
			for(int i=0;i<mNdList.getLength();i++) {
				Element m=(Element)mNdList.item(i);
				Material material=new Material((Element)(mNdList.item(i)));
				mats.put(material.name, material);
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Material getMaterial(String name){
		return mats.get(name);
	}
	/**
	 * 该初始化必须在ShaderLib初始化之后调用 仅可在openGL线程中调用
	 * @param shaderlib
	 */
	public void Init(ShaderLib shaderlib){
		Collection<Material>ms=mats.values();
		Iterator<Material>it=ms.iterator();
		while(it.hasNext()){
			Material m=it.next();
			m.setShader(shaderlib.getShader(m.shadername));
			m.InitHardwareBuffer();
		}
	}
}
