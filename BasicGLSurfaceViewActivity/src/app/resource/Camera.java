package app.resource;

import app.core.Float3;
import app.core.Matrix4f;

public abstract class Camera {
	
     float far;
     float near;
     //近视面的四边的数值
     float top;
     float bottom;
     float left;
     float right;
     /**投影矩阵*/
     Matrix4f projectionMatrix4f=new Matrix4f();
     /**世界坐标系到视点坐标系的矩阵*/
     Matrix4f viewMatrix4f=new Matrix4f();
     
     public void lookAt(float eyeX,float eyeY,float eyeZ,float centerX,float centerY,float centerZ,float upX,float upY,float upZ)
     {
    	 float fx,fy,fz;
    	 fx=centerX-eyeX;fy=centerY-eyeY;fz=centerZ-eyeZ;
    	 float length=(float)java.lang.Math.sqrt(fx*fx+fy*fy+fz*fz);
    	 if(java.lang.Math.abs(length-0.0f)>0.00001f)
    	 {
    		 length=1/length;
    		 fx*=length;fy*=length;fz*=length;
    	 }
    	 
    	 float Xx=fy*upZ-upY*fz;
    	 float Xy=fz*upX-fx*upZ;
    	 float Xz=fx*upY-fy*upX;
    	 
    	 length=(float)java.lang.Math.sqrt(Xx*Xx+Xy*Xy+Xz*Xz);
    	 if(java.lang.Math.abs(length-0.0f)>0.00001f)
    	 {
    		 length=1/length;
    		 Xx*=length;Xy*=length;Xz*=length;
    	 }
    	 
    	 upX=Xy*fz-Xz*fy;upY=Xz*fx-Xx*fz;upZ=Xx*fy-Xy*fx;
    	 
    	 float []m=viewMatrix4f.getArray();
    	 viewMatrix4f.loadIdentity();
    	 m[0]=Xx;
    	 m[1]=upX;
    	 m[2]=-fx;
    	 m[4]=Xy;
    	 m[5]=upY;
    	 m[6]=-fy;
    	 m[8]=Xz;
    	 m[9]=upZ;
    	 m[10]=-fz;
    	 
    	 translateInWorld(eyeX, eyeY, eyeZ);
     }
     /**
      * 这个向量是世界坐标系中的
      * @param x
      * @param y
      * @param z
      */
     public void translateInWorld(float x,float y,float z)
     {
    	 float m[]=viewMatrix4f.getArray();
    	 for(int i=0;i<3;i++){
    		 m[12+i]+=m[i]*-x+m[4+i]*-y+m[8+i]*-z;
    	 }
     }
     /**
      * 
      */
     public Float3 getPosition(){
    	 Float3 pos=new Float3();
    	 float m[]=viewMatrix4f.getArray();
    	 pos.x=-1*(m[0]*m[12]+m[1]*m[13]+m[2]*m[14]);
    	 pos.y=-1*(m[4]*m[12]+m[5]*m[13]+m[6]*m[14]);
    	 pos.z=-1*(m[8]*m[12]+m[9]*m[13]+m[10]*m[14]);
    	 return pos;
     }
     /**
      * 返回眼睛坐标系到世界坐标系中的变换矩阵
      */
     public Matrix4f getWorldMatrix(){
    	 Matrix4f wm=new Matrix4f(viewMatrix4f.getArray());
    	 float m[]=wm.getArray();
    	 m[12]=m[13]=m[14]=0.0f;
    	 wm.transpose();
    	 Float3 p=this.getPosition();
    	 m[12]=p.x;m[13]=p.y;m[14]=p.z;
    	 return wm;
     }
     
     /**
      * 设置摄像机在世界坐标系中 的位置
      * @param x
      * @param y
      * @param z
      */
     public void setPosition(float x,float y,float z)
     {
    	 float []m=viewMatrix4f.getArray();
    	 m[12]=m[13]=m[14]=0;
    	 for(int i=0;i<3;i++){
    		 m[12+i]=m[i]*-x+m[4+i]*-y+m[8+i]*-z;
    	 } 
     }
     
     /**
      * 让摄像机绕着当前的Up轴旋转theta角度 坐标系为左手坐标系则左手的大拇指指向正轴时四指的方向为正方向
      * @param theta
      */
     public void RotatebyLocalY(float theta)
     {
    	 Matrix4f rotation=new Matrix4f();
    	 rotation.loadIdentity();
    	 float c=(float)java.lang.Math.cos(theta);
    	 float s=(float)java.lang.Math.sin(theta);
    	 float []m=rotation.getArray();
    	 m[0]=c;m[2]=-s;
    	 m[8]=s;m[10]=c;
    	 viewMatrix4f.multiply(rotation);
     }
     /**
      * 让摄像机绕着固定的Up轴（默认为世界坐标系Y轴）旋转theta角度 坐标系为左手坐标系则左手的大拇指指向正轴时四指的方向为正方向
      * @param theta
      */
     public void RotatebyFixedY(float theta)
     {
    	 Matrix4f rotation=new Matrix4f();
    	 rotation.loadIdentity();
    	 float c=(float)java.lang.Math.cos(theta);
    	 float s=(float)java.lang.Math.sin(theta);
    	 float []m=rotation.getArray();
    	 m[0]=c;m[2]=-s;
    	 m[8]=s;m[10]=c;
    	 viewMatrix4f=Matrix4f.multiply(rotation,viewMatrix4f);
     }
     public void RotatebyLocalZ(float theta)
     {
    	 Matrix4f rotation=new Matrix4f();
    	 rotation.loadIdentity();
    	 float c=(float)java.lang.Math.cos(theta);
    	 float s=(float)java.lang.Math.sin(theta);
    	 float []m=rotation.getArray();
    	 m[0]=c;m[1]=s;
    	 m[4]=-s;m[5]=c;
    	 viewMatrix4f.multiply(rotation);
     }
     public void RotatebyLocalX(float theta)
     {
    	 Matrix4f rotation=new Matrix4f();
    	 rotation.loadIdentity();
    	 float c=(float)java.lang.Math.cos(theta);
    	 float s=(float)java.lang.Math.sin(theta);
    	 float []m=rotation.getArray();
    	 m[5]=c;m[6]=s;
    	 m[9]=-s;m[10]=c;
    	 viewMatrix4f.multiply(rotation);
     }
     
     public void RotatebyLocalAxis(float theta,float x,float y,float z)
     {
    	 Float3 a=new Float3(x,y,z);
    	 a.normalize();
    	 float c=(float)java.lang.Math.cos(theta);
    	 float s=(float)java.lang.Math.sin(theta);
    	 float nc=1.0f-c;
    	 float xyc=a.x*a.y*nc;
    	 float xzc=a.x*a.z*nc;
    	 float yzc=a.y*a.z*nc;
    	 float xs=a.x*s;float ys=a.y*s;float zs=a.z*s;
    	 Matrix4f rotateMatrix4f=new Matrix4f();
    	 float []m=rotateMatrix4f.getArray();
    	 m[0]=a.x*a.x*nc+c;
    	 m[1]=xyc+zs;
    	 m[2]=xzc-ys;
    	 m[4]=xyc-zs;
    	 m[5]=a.y*a.y*nc+c;
    	 m[6]=yzc+xs;
    	 m[8]=xzc+ys;
    	 m[9]=yzc-xs;
    	 m[10]=a.z*a.z*nc+c;
    	 viewMatrix4f.multiply(rotateMatrix4f);
     }
     
     /**投影矩阵与投影方式有关*/
     public abstract void calProjectMatrix();
     
     public void setFrustum(float left,float right,float bottom,float top,float near,float far)
     {
    	 this.left=left;
    	 this.right=right;
    	 this.bottom=bottom;
    	 this.top=top;
    	 this.near=near;
    	 this.far=far;
     }
     
     public void getWorldFrustum(){
    	 
     }
}
