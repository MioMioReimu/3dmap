package app.resource;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import app.core.AABB2D;
import app.core.Float3;
import app.core.Int2;
import app.core.Int3;
import app.core.Matrix4f;


public class Block {
	//经线对应的横轴的ID
	int xid;
	//纬度对应的纵轴的id
	int zid;
	//块id
	long id;
	
	static int EarthR=63781370;
	static double piR=Math.PI*EarthR;
	static int level=17;
	static double blockLength=Math.PI*EarthR/(1L<<level);
	//块在map坐标系中的位置
	Int3 mapPos;
	Float3 worldPos;
	//在世界坐标系中的包围盒
	public AABB2D wvb;
	
	HashMap<String,Drawable>elements;
	public Block(long id)
	{
		this.id=id;
		Int2 xzid=Block.calculatexzid(id);
		this.xid=xzid.x;
		this.zid=xzid.y;
		int x=(int)Block.calculatexybyid(xid);
		int z=(int)Block.calculatexybyid(zid);
		this.mapPos=new Int3(x,0,z);
		this.elements=new HashMap<String,Drawable>();
	}
	public static Block createBlockFromPath(long id,String path){
		DocumentBuilderFactory xmlFactory=DocumentBuilderFactory.newInstance();
		Block block=new Block(id);
		try {
			DocumentBuilder xmlBuilder=xmlFactory.newDocumentBuilder();
			Document doc=xmlBuilder.parse(new File(path));
			Element root=(Element)doc.getDocumentElement();
			NodeList ndList=root.getElementsByTagName("road");
			for(int i=0;i<ndList.getLength();i++) {
				Element element=(Element)(ndList.item(i));
				block.elements.put(element.getAttribute("id"), new Road(element, block));
			}
			return block;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}
	
	public static Int2 calculatexzid(long id)
	{
		Int2 xzid=new Int2();
		long j=0x0000000000000001L;
		int l=40;
		for(int i=0;i<l;i++)
		{
			if((i&0x00000001)==0)
				xzid.x+=(id&j)>>(i/2);
			else 
				xzid.y+=(id&j)>>((i+1)/2);
			j<<=1;
		}
		return xzid;
	}
	public static long calculateid(int xid,int zid)
	{
		long j=0x0000000000000001L;
		int l=20;
		long id=0;
		for(int i=0;i<l;i++)
		{
			id+=(xid&j)<<i;
			id+=(zid&j)<<(i+1);
			j<<=1;
		}
		return id;
	}
	public static double calculatexybyid(int xidorzid){
		return Math.PI*EarthR*((double)(xidorzid)/(1L<<(level-1))-1);
	}
	public static int calculatexidbyPos(int xorz){
		return (int)((xorz+piR)*(1<<(level-1))/piR);
	}
	public static double calculateBlockLength(int level){
		return Math.PI*EarthR/(1L<<level);
	}
	public static long calculateIDbyMappos(Int2 pos){
		return 0;
	}
	/**
	 * 
	 * @return 世界坐标系中块的包围盒
	 */
	public AABB2D getAABB2D(){
		return wvb;
	}
	public Float3 getPosInWorld(){
		return worldPos;
	}
	/**
	 * 更新块在世界坐标系中的信息
	 * @param refPos
	 */
	public void updateInfoInWorld(Int3 refPos){
		 //计算在世界坐标系中的位置
		this.worldPos=new Float3(mapPos.x-refPos.x, mapPos.y-refPos.y, mapPos.z-refPos.z);
		//计算在世界坐标系中的包围盒
		float halflen=(float)(blockLength*0.5);
		this.wvb=new AABB2D(worldPos.x+halflen,worldPos.z+halflen,halflen,halflen);
	}
	
	public boolean addElement(Drawable p)
	{
		if(p==null)return false;
		this.elements.put(p.getid(), p);
		return true;
	}
	
	/**
	 * 
	 * @return 返回在map坐标系中的位置
	 */
	public Int3 getPosInMap(){
		return mapPos;
	}
	public Matrix4f getBlockMatrix(){
		Matrix4f m=new Matrix4f();
		m.loadTranslate(this.worldPos.x, this.worldPos.y, this.worldPos.z);
		return m;
	}
}
