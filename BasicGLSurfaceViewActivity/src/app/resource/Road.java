package app.resource;

import java.util.Iterator;

import app.core.AABB2D;
import app.core.Float3;
import app.core.Int3;


public class Road extends Drawable {

	private String name;
	public AABB2D vb;
	private Block parent;
	public Road(String id,Block parent) {
		super(id);
		this.parent=parent;
		// TODO Auto-generated constructor stub
	}

	public Road(org.w3c.dom.Element e,Block parent) {
		super(e.getAttribute("id"));
		this.parent=parent;
		this.initByXmlNode(e);
		this.calculateAABB();
	}

	public boolean initByXmlNode(org.w3c.dom.Element e) {
		this.id = e.getAttribute("id");
		this.name = e.getAttribute("name");
		org.w3c.dom.NodeList triNdList = e.getElementsByTagName("triangles");
		for (int i = 0; i < triNdList.getLength(); i++) {
			org.w3c.dom.Element tris = (org.w3c.dom.Element) triNdList.item(i);
			if (tris.hasChildNodes())
				this.addMesh(new Mesh(tris));
		}
		return true;
	}
	/**
	 * 该函数计算该道路在Block坐标系中的包围矩形
	 */
	public void calculateAABB(){
		float minx=Float.MAX_VALUE;
		float minz=Float.MAX_VALUE;
		float maxx=Float.MIN_VALUE;
		float maxz=Float.MIN_VALUE;
		Iterator<Mesh>meshIt=meshes.iterator();
		while(meshIt.hasNext()){
			Mesh m=meshIt.next();
			float marray[]=m.vertexData.array();
			int vtxSize=m.getVertexSize();
			for(int i=0;i<marray.length;i+=vtxSize){
				if(marray[i]>maxx)
					maxx=marray[i];
				if(marray[i]<minx)
					minx=marray[i];
				if(marray[i+2]>maxz)
					maxz=marray[i+2];
				if(marray[i+2]<minz)
					minz=marray[i+2];
			}
		}
		maxx=(float) ((maxx-minx)*0.5);
		maxz=(float) ((maxz-minz)*0.5);
		this.vb=new AABB2D((int)(minx+maxx),(int)(minz+maxz),(int)maxx,(int)maxz);
	}
	
	public AABB2D getBlockBoundingBox(){
		return vb;
	}
	public AABB2D getWorldBoundingBox(){
		Float3 pos=this.parent.getPosInWorld();
		AABB2D wbb=new AABB2D(pos.x+vb.x,pos.z+vb.y,vb.halfx,vb.halfy);
		return wbb;
	}
}
