package app.resource;

import android.opengl.GLES20;
import android.util.Log;

public class Shader {
	public	int handle;
	public int vertexHandle;
	public int uvHandle[]=new int[10];
	public int normalHandle;
	public int modelMatrixHandle;
	public int ViewProjectionMatrixHandle;
	Shader(String vtxsrc,String fragsrc,int uvNum,boolean hasnormal){
		int vertexShader=this.createShader(vtxsrc, GLES20.GL_VERTEX_SHADER);
		assert(vertexShader!=0);
		int fragShader=this.createShader(fragsrc, GLES20.GL_FRAGMENT_SHADER);
		assert(fragShader!=0);
		int program=GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragShader);
		GLES20.glLinkProgram(program);
		int[] linkStatus=new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("Link Shader", "Could not link program: ");
            Log.e("Link Shader", GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
		this.handle=program;
		
		assert(this.handle!=0);
		this.vertexHandle=GLES20.glGetAttribLocation(this.handle, "vertex");
		for(int i=0;i<uvNum;i++)
			this.uvHandle[i]=GLES20.glGetAttribLocation(this.handle, "uv"+Integer.toString(i));
		if(hasnormal){
			this.normalHandle=GLES20.glGetAttribLocation(this.handle, "nml");
		}
		this.modelMatrixHandle=GLES20.glGetUniformLocation(this.handle, "mMatrix");
		this.ViewProjectionMatrixHandle=GLES20.glGetUniformLocation(this.handle, "vpMatrix");
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
	
}
