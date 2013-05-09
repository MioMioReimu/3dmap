package app.resource;

import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.util.Log;

/**
 * shader编写须知:
 * 顶点位置变量名必须设置为 aPosition
 * 纹理坐标变量名必须设置为aTextureCoord
 * 顶点颜色变量名必须设置为aColor
 * 顶点法线必须设置为             aNormalCoord
 * 模型矩阵                                   uMMatrix
 * 视图矩阵                                   uVMatrix
 * 投影矩阵                                   uPMatrix
 * 总矩阵                                        uMVPMatrix
 * 视图投影矩阵                          uVPMatrix
 * 
 * @author tlm
 *
 */
public class Shader {
	public	int programHandle;
	public int vshaderHandle;
	public int fshaderHandle;
	public String name;
	public int aPositionHandle;
	public int aTextureCoordHandle;
	public int aColorHandle;
	public int aNormalCoordHandle;
	public int uMMatrixHandle;
	public int uVMatrixHandle;
	public int uPMatrixHandle;
	public int uMVPMatrixHandle;
	public int uVPMatrixHandle;
	public int uCameraPositionHandle;
	public int samplerHandle[];
	public int samplerNum;
	public SamplerType[] samplertype;
	
	Shader(String vtxsrc,String fragsrc,String name){
		this.vshaderHandle=this.createShader(vtxsrc, GLES20.GL_VERTEX_SHADER);
		assert(vshaderHandle!=0);
		this.fshaderHandle=this.createShader(fragsrc, GLES20.GL_FRAGMENT_SHADER);
		assert(fshaderHandle!=0);
		int program=GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vshaderHandle);
		GLES20.glAttachShader(program, fshaderHandle);
		GLES20.glLinkProgram(program);
		int[] linkStatus=new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("Link Shader", "Could not link program: ");
            Log.e("Link Shader", GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
		this.programHandle=program;
		
		assert(this.programHandle!=0);
		setDefaultHandles();
		this.name=name;
	}
	public void setDefaultHandles()
	{
		this.aPositionHandle=getAttribHandle("aPosition");
		this.aTextureCoordHandle=getAttribHandle("aTextureCoord");
		this.aColorHandle=getAttribHandle("aColorHandle");
		this.aNormalCoordHandle=getAttribHandle("aNormalCoord");
		this.uMMatrixHandle=getUniformHandle("uMMatrix");
		this.uVMatrixHandle=getUniformHandle("uVMatrix");
		this.uPMatrixHandle=getUniformHandle("uPMatrix");
		this.uMVPMatrixHandle=getUniformHandle("uMVPMatrix");
		this.uVPMatrixHandle=getUniformHandle("uVPMatrix");
		this.uCameraPositionHandle=getUniformHandle("uCameraPositionHandle");
	}
	/**
	 * 返回shader中输入变量对应的句柄，shader中没有该变量 则返回-1
	 * @param name
	 * @return
	 */
	public int getAttribHandle(String name)
	{
		return GLES20.glGetAttribLocation(this.programHandle, name);
	}
	public int getUniformHandle(String name)
	{
		return GLES20.glGetUniformLocation(this.programHandle, name);
	}
	
	private int createShader(String src,int shadertype){
		int shader=GLES20.glCreateShader(shadertype);
		if(shader!=0){
			GLES20.glShaderSource(shader, src);
			GLES20.glCompileShader(shader);
			int []compiled=new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
                Log.e("Create Shader", "Could not compile shader " + shadertype + ":");
                Log.e("Create Shader",GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
		}
		return shader;
	}
	
	public void setTextureNum(int num,String []names,String[]type)
	{
		this.samplerNum=num;
		this.samplerHandle=new int[num];
		this.samplertype=new SamplerType[num];
		for(int i=0;i<num;i++)
		{
			this.samplerHandle[i]=GLES20.glGetUniformLocation(this.programHandle, names[i]);
			if(type[i].equals("sampler2D")){
				this.samplertype[i]=SamplerType.sampler2D;
			}
			//TODO:增加其他的类型的sampler
		}
	}
	
	
	public void useShader()
	{
		GLES20.glUseProgram(programHandle);
	}
	public void setVertices(int bufferid,int stride,int offset){
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferid);
		GLES20.glEnableVertexAttribArray(this.aPositionHandle);
		GLES20.glVertexAttribPointer(this.aPositionHandle, 3, GLES20.GL_FLOAT, false, stride, offset);
	}
	public void setVertices(FloatBuffer buffer,int stride,int offset){
		GLES20.glEnableVertexAttribArray(this.aPositionHandle);
		buffer.position(offset/4);
		GLES20.glVertexAttribPointer(this.aPositionHandle, 3, GLES20.GL_FLOAT, false, stride, buffer);
	}
	public void setTexCoord(int bufferid,int stride,int offset){
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferid);
		GLES20.glEnableVertexAttribArray(this.aTextureCoordHandle);
		GLES20.glVertexAttribPointer(this.aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, stride, offset);
	}
	public void setTexCoord(FloatBuffer buffer,int stride,int offset){
		GLES20.glEnableVertexAttribArray(this.aTextureCoordHandle);
		buffer.position(offset/4);
		GLES20.glVertexAttribPointer(this.aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, stride, buffer);
	}
	
	public void setModelMatrix(float[] m){
		GLES20.glUniformMatrix4fv(this.uMMatrixHandle, 1, false, m, 0);
	}
	public void setViewMatrix(float[]m){
		GLES20.glUniformMatrix4fv(this.uVMatrixHandle, 1, false, m, 0);
	}
	public void setProjectionMatrix(float[]m){
		GLES20.glUniformMatrix4fv(this.uPMatrixHandle, 1, false, m, 0);
	}
	public void setMVPMatrix(float[]m) {
		GLES20.glUniformMatrix4fv(this.uMVPMatrixHandle, 1, false, m, 0);
	}
	public void setVPMatrix(float[]m) {
		GLES20.glUniformMatrix4fv(this.uVPMatrixHandle, 1, false, m, 0);
	}
	/**
	 * 
	 * @param ts
	 */
	public void bindTextures(Texture[]ts){
		for(int i=0;i<this.samplerNum;i++) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0+i);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ts[i].id);
			GLES20.glUniform1i(samplerHandle[i], i);
		}
	}
	public void unbindTextures(Texture[]ts){
		for(int i=0;i<this.samplerNum;i++){
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}
	}
	public static int VAR_ATTRIB=0;
	public static int VAR_UNIFORM=1;
	public enum SamplerType{
		sampler2D,
		//Sampler3D,
	};
}
