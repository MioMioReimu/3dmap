package app.resource;

public class PersCamera extends Camera {
	
	/**以另一种方式设置frustum*/
	void persective(float FOVy,float aspect,float near,float far)
	{
		this.top=(float) (near*Math.tan(FOVy*(Math.PI/360.0)));
		this.bottom=-top;
		this.right=top*aspect;
		this.left=-right;
		this.far=far;
		this.near=near;
		this.calProjectMatrix();
	}
	@Override
	public void calProjectMatrix()
	{
		float inverseWidth=1/(right-left);
		float inverseHeight=1/(top-bottom);
		float inverseDepth=1/(far-near);
		float m00=2*near*inverseWidth;
		float m20=(right+left)*inverseWidth;
		float m11=2*near*inverseHeight;
		float m21=(top+bottom)*inverseHeight;
		float m22=-1*(far+near)*inverseDepth;
		float m32=-2*(far*near)*inverseDepth;
		float []m=projectionMatrix4f.getArray();
		m[0]=m00;
		m[1]=m[2]=m[3]=m[4]=0;
		m[5]=m11;
		m[6]=m[7]=0;
		m[8]=m20;
		m[9]=m21;
		m[10]=m22;
		m[11]=-1;
		m[12]=m[13]=0;
		m[14]=m32;
		m[15]=0.0f;
	}
}
