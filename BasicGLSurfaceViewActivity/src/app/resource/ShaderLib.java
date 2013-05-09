package app.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;
public class ShaderLib {
	private final static String shaderlibPath="/sdcard/map/shaderlib/shaderlib.xml";
	protected HashMap<String,Shader>shaders;
	public Shader getShader(String name){
		return shaders.get(name);
	}
	public ShaderLib()
	{
		shaders=new HashMap<String, Shader>();
		DocumentBuilderFactory xmlFactory=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder xmlBuilder=xmlFactory.newDocumentBuilder();
			Document doc=xmlBuilder.parse(new File(shaderlibPath));
			Element root=(Element)doc.getDocumentElement();
			NodeList sndlist=root.getElementsByTagName("shader");
			for(int i=0;i<sndlist.getLength();i++){
				Element shaderNd=(Element)sndlist.item(i);
				
				String shadername=shaderNd.getAttribute("name");
				String vsSrc=readShader(((Element)shaderNd.getElementsByTagName("vs").item(0)).getAttribute("path"));
				String fsSrc=readShader(((Element)shaderNd.getElementsByTagName("fs").item(0)).getAttribute("path"));
				Shader s=new Shader(vsSrc, fsSrc, shadername);
				
				//设置shader中使用的sampler
				NodeList samplerlist=shaderNd.getElementsByTagName("sampler");
				String[]samplertypes=new String[samplerlist.getLength()];
				String[]samplernames=new String[samplerlist.getLength()];
				for(int j=0;j<samplerlist.getLength();j++) {
					Element sampler=(Element)samplerlist.item(j);
					String type=sampler.getAttribute("type");
					samplertypes[j]=type;
					String samplername=sampler.getAttribute("name");
					samplernames[j]=samplername;
				}
				s.setTextureNum(samplerlist.getLength(), samplernames, samplertypes);
				shaders.put(shadername, s);
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
	private String readShader(String path){
		File f=new File(path);
		if(f.isDirectory()) {
			Log.e("readShader:", "file path is invalid!");
			return "";
		}
		try {
			String text="";
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line;
			while((line=reader.readLine())!=null){
				text+=line+'\n';
			}
			reader.close();
			return text;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
