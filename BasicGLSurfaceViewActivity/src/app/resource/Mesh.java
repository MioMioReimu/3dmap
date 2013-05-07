package app.resource;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.R.integer;
import android.opengl.GLES20;
import android.util.Log;

public class Mesh {
	int type;
	String materialName;
	FloatBuffer vertexData;
	static final int TRIFAN = 0x00000006;
	static final int TRISTRIP = 0x00000005;
	static final int TRIMODEANDER=0x00000007;
	static final int TRILIST = 0x00000004;
	
	static final int HASUV = 0x00000010;
	static final int HASNORMAL = 0x00000020;
	static final int USEHARDWAREBUFFER=0x00000040;
	private int vertexSize;
	private int triSize;
	
	IntBuffer hwVtxBuffID=IntBuffer.allocate(1);
	
	public Mesh(int type, float buffer[]) {
		this.type = type;
		this.vertexData = FloatBuffer.wrap(buffer);
		
		calculateVertexSize();
		this.calculateTriSize();
	}

	public Mesh(int type, FloatBuffer buf) {
		this.type = type;
		this.vertexData = buf;
		
		calculateVertexSize();
		this.calculateTriSize();
	}
	private void calculateVertexSize(){
		this.vertexSize=3;
		if((type&HASUV)==HASUV)
			vertexSize+=2;
		if((type&HASNORMAL)==HASNORMAL)
			vertexSize+=3;
	}
	private void calculateTriSize(){
		this.triSize=vertexData.capacity()/this.vertexSize;
	}
	public int getVertexSize(){
		return vertexSize;
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
		this.vertexSize=NdSize;
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
		
		this.calculateTriSize();
	}
	
	/**
	 * 该函数只能在render线程中调用
	 * 把该物体的顶点数据送入显卡顶点缓冲区
	 */
	public boolean InitHardwareBuffer(){
		
		GLES20.glGenBuffers(1, this.hwVtxBuffID);
		assert(hwVtxBuffID.get(0)!=0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.hwVtxBuffID.get(0));
		this.vertexData.position(0);
		//第一个参数 类型 顶点缓冲区或者索引缓冲区
		//第二个参数 复制到缓冲区的Buffer的字节数，注意是字节数
		//第三个参数 Buffer指针
		//第四个参数 缓冲区类型
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.vertexData.capacity()*4, this.vertexData, GLES20.GL_STATIC_DRAW);
		if(this.checkGLError("Allocate VBO ")){
			this.type|=Mesh.USEHARDWAREBUFFER;
			return true;
		}
		this.hwVtxBuffID.put(0, 0);
		return false;
	}
	
	public void Render(Shader s,Material m,float mMatrix[],float[] vpMatrix){
		GLES20.glUseProgram(s.handle);
		if((this.type&Mesh.USEHARDWAREBUFFER)==Mesh.USEHARDWAREBUFFER){
			this.RenderWithHardwareBuffer(s, m, mMatrix, vpMatrix);
		}else {
			if(this.InitHardwareBuffer()){
				this.RenderWithHardwareBuffer(s, m, mMatrix, vpMatrix);
			}else{
				RenderWithLocalBuffer(s,m,mMatrix,vpMatrix);
			}
		}
	}
	private void RenderWithHardwareBuffer(Shader s,Material m,float []mMatrix,float []vpMatrix){
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.hwVtxBuffID.get(0));
		
		GLES20.glVertexAttribPointer(s.vertexHandle, 3, GLES20.GL_FLOAT, false, this.vertexSize*4, 0);
		GLES20.glEnableVertexAttribArray(s.vertexHandle);
		if((this.type&Mesh.HASUV)==Mesh.HASUV){
			GLES20.glVertexAttribPointer(s.uvHandle[0], 2, GLES20.GL_FLOAT, false, this.vertexSize*4, 12);
		}
		GLES20.glUniformMatrix4fv(s.modelMatrixHandle, 1, false, mMatrix,0);
		GLES20.glUniformMatrix4fv(s.ViewProjectionMatrixHandle, 1, false, vpMatrix,0);
		//Log.e("Mesh:",String.valueOf(this.type&Mesh.TRIMODEANDER));
		GLES20.glDrawArrays(this.type&Mesh.TRIMODEANDER, 0, this.triSize);
		checkGLError("DrawTriangles");
	}
	private void RenderWithLocalBuffer(Shader s,Material m,float [] mMatrix,float [] vpMatrix){
		GLES20.glEnableVertexAttribArray(s.vertexHandle);
		int offset=0;
		this.vertexData.position(offset);
		GLES20.glVertexAttribPointer(s.vertexHandle, 3, GLES20.GL_FLOAT, false, this.vertexSize*4, this.vertexData);
		offset+=3;
		if((this.type&Mesh.HASUV)==Mesh.HASUV){
			this.vertexData.position(offset);
			GLES20.glVertexAttribPointer(s.uvHandle[0], 2, GLES20.GL_FLOAT, false, this.vertexSize*4, this.vertexData);
			offset+=2;
		}
		GLES20.glUniformMatrix4fv(s.modelMatrixHandle, 1, false, mMatrix,0);
		GLES20.glUniformMatrix4fv(s.ViewProjectionMatrixHandle, 1, false, vpMatrix,0);
		GLES20.glDrawArrays(this.type&Mesh.TRIMODEANDER, 0, this.triSize);
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
