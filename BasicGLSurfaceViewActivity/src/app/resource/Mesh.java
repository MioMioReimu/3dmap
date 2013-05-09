package app.resource;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import android.opengl.GLES20;
import android.util.Log;

public class Mesh {
	int type;
	String materialName;
	Material material=null;
	FloatBuffer vertexData;
	static final int TRIFAN = 0x00000006;
	static final int TRISTRIP = 0x00000005;
	static final int TRIMODEANDER=0x00000007;
	static final int TRILIST = 0x00000004;
	
	static final int HASUV = 0x00000010;
	static final int HASNORMAL = 0x00000020;
	static final int USEHARDWAREBUFFER=0x00000040;
	private int strideSize;
	private int vertexSize;
	private int hardwareBufferid=0;
	
	
	public Mesh(int type, float buffer[]) {
		this.type = type;
		this.vertexData = FloatBuffer.wrap(buffer);
		
		calculateStrideSize();
		this.calculateVertexSize();
	}

	public Mesh(int type, FloatBuffer buf) {
		this.type = type;
		this.vertexData = buf;
		
		calculateStrideSize();
		this.calculateVertexSize();
	}
	private void calculateStrideSize(){
		this.strideSize=3;
		if((type&HASUV)==HASUV)
			strideSize+=2;
		if((type&HASNORMAL)==HASNORMAL)
			strideSize+=3;
	}
	private void calculateVertexSize(){
		this.vertexSize=vertexData.capacity()/this.strideSize;
	}
	public int getStrideSize(){
		return strideSize;
	}
	public void setMaterial(Material m){
		this.material=m;
	}
	public void setMaterial(MaterialLib mlib){
		
		this.material=mlib.getMaterial(materialName);
	}
	public Mesh(org.w3c.dom.Element e) {
		String type = e.getAttribute("type");
		if (type.equals("list")) {
			this.type = Mesh.TRILIST;
		} else if (type.equals("strip")) {
			this.type = Mesh.TRISTRIP;
		} else {
			assert (type.equals("fan"));
			this.type = Mesh.TRIFAN;
		}
		this.materialName=e.getAttribute("material");
		int NdSize=3;
		org.w3c.dom.NodeList ndList=e.getElementsByTagName("node");
		org.w3c.dom.Element nd=(org.w3c.dom.Element)ndList.item(0);
		/**判断是否有纹理坐标和法线坐标*/
		if(nd.hasAttribute("u"))
		{
			NdSize+=2;
			this.type|=Mesh.HASUV;
		}
		if(nd.hasAttribute("nx"))
		{
			NdSize+=3;
			this.type|=Mesh.HASNORMAL;
		}
		this.strideSize=NdSize;
		float buffer[]=new float[ndList.getLength()*NdSize];
		if(NdSize==5){
			int k=0;
			for(int i=0;i<ndList.getLength();i++) {
				nd=(org.w3c.dom.Element)ndList.item(i);
				buffer[k++]=Float.parseFloat(nd.getAttribute("x"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("y"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("z"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("u"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("v"));
			}
		} else if(NdSize==8) {
			int k=0;
			for(int i=0;i<ndList.getLength();i++) {
				nd=(org.w3c.dom.Element)ndList.item(i);
				buffer[k++]=Float.parseFloat(nd.getAttribute("x"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("y"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("z"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("u"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("v"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("nx"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("ny"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("nz"));
			}
		} else {
			int k=0;
			for(int i=0;i<ndList.getLength();i++) {
				nd=(org.w3c.dom.Element)ndList.item(i);
				buffer[k++]=Float.parseFloat(nd.getAttribute("x"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("y"));
				buffer[k++]=Float.parseFloat(nd.getAttribute("z"));
			}
		}
		this.vertexData=FloatBuffer.wrap(buffer);
		
		this.calculateVertexSize();
	}
	
	/**
	 * 该函数只能在render线程中调用
	 * 把该物体的顶点数据送入显卡顶点缓冲区
	 */
	public boolean InitHardwareBuffer(){
		IntBuffer hwVtxBuffID=IntBuffer.allocate(1);
		GLES20.glGenBuffers(1, hwVtxBuffID);
		assert(hwVtxBuffID.get(0)!=0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, hwVtxBuffID.get(0));
		this.vertexData.position(0);
		//第一个参数 类型 顶点缓冲区或者索引缓冲区
		//第二个参数 复制到缓冲区的Buffer的字节数，注意是字节数
		//第三个参数 Buffer指针
		//第四个参数 缓冲区类型
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.vertexData.capacity()*4, this.vertexData, GLES20.GL_STATIC_DRAW);
		if(this.checkGLError("Allocate VBO ")){
			this.type|=Mesh.USEHARDWAREBUFFER;
			this.hardwareBufferid=hwVtxBuffID.get(0);
			return true;
		}
		this.hardwareBufferid=0;
		return false;
	}
	
	public void Render(MaterialLib mlib,float mMatrix[],float[] vpMatrix){
		if(this.material==null){
			this.setMaterial(mlib.getMaterial(materialName));
		}
		if((this.type&Mesh.USEHARDWAREBUFFER)==Mesh.USEHARDWAREBUFFER){
			this.RenderWithHardwareBuffer( mMatrix, vpMatrix);
		}else {
			if(this.InitHardwareBuffer()){
				this.RenderWithHardwareBuffer(mMatrix, vpMatrix);
			}else{
				RenderWithLocalBuffer(mMatrix,vpMatrix);
			}
		}
	}
	private void RenderWithHardwareBuffer(float []mMatrix,float []vpMatrix){
		Shader s=this.material.shader;
		s.useShader();
		s.bindTextures(material.getTextures());
		checkGLError("use program");
		s.setVertices(hardwareBufferid,this.strideSize*4 , 0);
		if((this.type&HASUV)==HASUV){
			s.setTexCoord(hardwareBufferid, this.strideSize*4, 12);
		}
		if((this.type&HASNORMAL)==HASNORMAL){
			//
		}
		
		s.setVPMatrix(vpMatrix);
		s.setModelMatrix(mMatrix);
		GLES20.glDrawArrays(this.type&TRIMODEANDER, 0, this.vertexSize);
		checkGLError("DrawTriangles");
	}
	private void RenderWithLocalBuffer(float [] mMatrix,float [] vpMatrix){
		Shader s=this.material.shader;
		GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		s.useShader();
		//s.bindTextures(material.getTextures());
		s.setVertices(this.vertexData,this.strideSize*4 , 0);
		if((this.type&HASUV)==HASUV){
			s.setTexCoord(this.vertexData, this.strideSize*4, 12);
		}
		if((this.type&HASNORMAL)==HASNORMAL){
			//
		}
		
		s.setVPMatrix(vpMatrix);
		s.setModelMatrix(mMatrix);
		GLES20.glDrawArrays(this.type&TRIMODEANDER, 0, this.vertexSize);
		checkGLError("DrawTriangles");
	}
	private boolean checkGLError(String op){
		int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("openGL es ", op + ": glError " + error);
            return false;
        }
        return true;
	}
}
