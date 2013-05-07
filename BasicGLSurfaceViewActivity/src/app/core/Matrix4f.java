package app.core;

public class Matrix4f {
		/**
		 * 本矩阵属于行距阵 行优先存储的矩阵
		 * 需要区别的是openGL使用的是列矩阵按列存储
		 * viewPos=pos*ModelMatrix*ViewMatrix*ProjectionMatrix
		 * android.opengl.Matrix.multiplyMM()函数认为传递的矩阵是按列存储 例如你传递的参数是a[16]
		 * 它认为是如下的矩阵
		 * [0,4,8, 12]
		 * [1,5,9, 13]
		 * [2,6,10,14]
		 * [3,7,11,15]
		 *               [x1,y1,z1,0]
		 * [x,y,z,1] * [x2,y2,z2,0]=
		 *               [x3,y3,z3,0]
		 *               [xt, yt, zt,1]
		 */
		final float []m;
		public Matrix4f(){
			m=new float[16];
			loadIdentity();
		}
		public Matrix4f(float[] data){
			m=new float[16];
			System.arraycopy(data, 0, m, 0, m.length);
		}
		public float []getArray()
		{
			return m;
		}
		
		public void loadIdentity(){
			m[0]=1;m[1]=0;m[2]=0;m[3]=0;
			m[4]=0;m[5]=1;m[6]=0;m[7]=0;
			m[8]=0;m[9]=0;m[10]=1;m[11]=0;
			m[12]=0;m[13]=0;m[14]=0;m[15]=1;
		}
		public void loadRotate(float rotation,float x,float y,float z){
			/**
			 * p'=(p-(p*n)n)cosr+(nxp)sinr+(p*n)n
			 * [x*x(1-cosr)+cosr   xy(1-cosr)+zsinr   xz(1-cosr)-ysinr   ]
			 * [xy(1-cosr)-zsinr     y*y(1-cosr)+cosr  yz(1-cosr)+xsinr  ]
			 * [xz(1-cosr)+ysinr    yz(1-cosr)-xsinr    z*z(1-cosr)+cosr ]
			 * 
			 */
			float cosr,sinr;
			rotation=(float)(java.lang.Math.PI/180.0f);
			cosr=(float)java.lang.Math.cos(rotation);
			sinr=(float)java.lang.Math.sin(rotation);
			
			float length=(float)java.lang.Math.sqrt(x*x+y*y+z*z);
			if(java.lang.Math.abs(length-1.0f)>0.00001f)
			{
				length=1.0f/length;
				x*=length;
				y*=length;
				z*=length;
			}
			float nc=1.0f-cosr;
			float xyc=x*y*nc;
			float xzc=x*z*nc;
			float yzc=y*z*nc;
			float zs=z*sinr;
			float ys=y*sinr;
			float xs=x*sinr;
			m[0]=x*x*nc+cosr;
			m[1]=xyc+zs;
			m[2]=xzc-ys;
			m[4]=xyc-zs;
			m[5]=y*y*nc+cosr;
			m[6]=yzc+xs;
			m[8]=xzc+ys;
			m[9]=yzc-xs;
			m[10]=z*z*nc+cosr;
			m[3]=0;
			m[7]=0;
			m[11]=m[12]=m[13]=m[14]=0;
			m[15]=1;
		}
		
		public void loadTranslate(float x,float y,float z)
		{
			loadIdentity();
			m[12]=x;
			m[13]=y;
			m[14]=z;
		}
		public void loadScale(float x,float y,float z)
		{
			loadIdentity();
			m[0]=x;
			m[5]=y;
			m[10]=z;
		}
		
		/**
		 * return=a*b;
		 * @param a 
		 * @param b
		 * @return
		 */
		public static Matrix4f multiply(Matrix4f a,Matrix4f b)
		{
			Matrix4f r=new Matrix4f();
			for(int i=0;i<4;i++)
			{
				float i0=0,i1=0,i2=0,i3=0;
				for(int j=0;j<4;j++)
				{
					float aij=a.m[i*4+j];
					i0+=aij*b.m[j*4+0];
					i1+=aij*b.m[j*4+1];
					i2+=aij*b.m[j*4+2];
					i3+=aij*b.m[j*4+3];
				}
				r.m[i*4]=i0;
				r.m[i*4+1]=i1;
				r.m[i*4+2]=i2;
				r.m[i*4+3]=i3;
			}
			
			return r;
		}
		
		/**
		 * 
		 * a=a*b
		 * @param b
		 */
		public void multiply(Matrix4f b)
		{
			float [] r=new float[16];
			for(int i=0;i<4;i++)
			{
				float i0=0,i1=0,i2=0,i3=0;
				for(int j=0;j<4;j++)
				{
					float aij=m[i*4+j];
					i0+=aij*b.m[j*4+0];
					i1+=aij*b.m[j*4+1];
					i2+=aij*b.m[j*4+2];
					i3+=aij*b.m[j*4+3];
				}
				r[i*4]=i0;
				r[i*4+1]=i1;
				r[i*4+2]=i2;
				r[i*4+3]=i3;
			}
			System.arraycopy(r, 0, m, 0, m.length);
		}
		private float computeCofactor(int i, int j) {
	        int c0 = (i+1) % 4;
	        int c1 = (i+2) % 4;
	        int c2 = (i+3) % 4;
	        int r0 = (j+1) % 4;
	        int r1 = (j+2) % 4;
	        int r2 = (j+3) % 4;

	        float minor = (m[c0 + 4*r0] * (m[c1 + 4*r1] * m[c2 + 4*r2] -
	                                            m[c1 + 4*r2] * m[c2 + 4*r1]))
	                     - (m[c0 + 4*r1] * (m[c1 + 4*r0] * m[c2 + 4*r2] -
	                                            m[c1 + 4*r2] * m[c2 + 4*r0]))
	                     + (m[c0 + 4*r2] * (m[c1 + 4*r0] * m[c2 + 4*r1] -
	                                            m[c1 + 4*r1] * m[c2 + 4*r0]));

	        float cofactor = ((i+j) & 1) != 0 ? -minor : minor;
	        return cofactor;
	    }
		
		/**
		 * 求逆
		 * @return 是否有逆矩阵
		 */
		public boolean inverse(){
			Matrix4f result = new Matrix4f();

	        for (int i = 0; i < 4; ++i) {
	            for (int j = 0; j < 4; ++j) {
	                result.m[4*i + j] = computeCofactor(i, j);
	            }
	        }

	        // Dot product of 0th column of source and 0th row of result
	        float det = m[0]*result.m[0] + m[4]*result.m[1] +
	                     m[8]*result.m[2] + m[12]*result.m[3];

	        if (Math.abs(det) < 1e-6) {
	            return false;
	        }

	        det = 1.0f / det;
	        for (int i = 0; i < 16; ++i) {
	            m[i] = result.m[i] * det;
	        }

	        return true;
		}
		/**
		 * 逆矩阵+转置
		 * @return 返回是否有逆矩阵
		 */
		public boolean inverseTranspose() {

	        Matrix4f result = new Matrix4f();

	        for (int i = 0; i < 4; ++i) {
	            for (int j = 0; j < 4; ++j) {
	                result.m[4*j + i] = computeCofactor(i, j);
	            }
	        }

	        float det = m[0]*result.m[0] + m[4]*result.m[4] +
	                     m[8]*result.m[8] + m[12]*result.m[12];

	        if (Math.abs(det) < 1e-6) {
	            return false;
	        }

	        det = 1.0f / det;
	        for (int i = 0; i < 16; ++i) {
	            m[i] = result.m[i] * det;
	        }

	        return true;
	    }
		
		/**
		 * 转置
		 */
		public void transpose() {
	        for(int i = 0; i < 3; ++i) {
	            for(int j = i + 1; j < 4; ++j) {
	                float temp = m[i*4 + j];
	                m[i*4 + j] = m[j*4 + i];
	                m[j*4 + i] = temp;
	            }
	        }
	    }
}
