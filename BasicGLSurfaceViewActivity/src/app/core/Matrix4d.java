package app.core;

public class Matrix4d {
	/**
	 * ±¾¾ØÕóÊôÓÚÐÐ¾àÕó °´ÐÐ´æ´¢µÄ¾ØÕó
	 *               [x1,y1,z1,0]
	 * [x,y,z,1] * [x2,y2,z2,0]=
	 *               [x3,y3,z3,0]
	 *               [xt, yt, zt,1]
	 */
	final double []m;
	public Matrix4d(){
		m=new double[16];
		loadIdentity();
	}
	public Matrix4d(double[] data){
		m=new double[16];
		System.arraycopy(data, 0, m, 0, m.length);
	}
	public double []getArray()
	{
		return m;
	}
	
	public void loadIdentity(){
		m[0]=1;m[1]=0;m[2]=0;m[3]=0;
		m[4]=0;m[5]=1;m[6]=0;m[7]=0;
		m[8]=0;m[9]=0;m[10]=1;m[11]=0;
		m[12]=0;m[13]=0;m[14]=0;m[15]=1;
	}
	public void loadRotate(double rotation,double x,double y,double z){
		/**
		 * p'=(p-(p*n)n)cosr+(nxp)sinr+(p*n)n
		 * [x*x(1-cosr)+cosr   xy(1-cosr)+zsinr   xz(1-cosr)-ysinr   ]
		 * [xy(1-cosr)-zsinr     y*y(1-cosr)+cosr  yz(1-cosr)+xsinr  ]
		 * [xz(1-cosr)+ysinr    yz(1-cosr)-xsinr    z*z(1-cosr)+cosr ]
		 * 
		 */
		double cosr,sinr;
		rotation=(double)(java.lang.Math.PI/180.0f);
		cosr=(double)java.lang.Math.cos(rotation);
		sinr=(double)java.lang.Math.sin(rotation);
		
		double length=(double)java.lang.Math.sqrt(x*x+y*y+z*z);
		if(java.lang.Math.abs(length-1.0f)>0.00001f)
		{
			length=1.0f/length;
			x*=length;
			y*=length;
			z*=length;
		}
		double nc=1.0f-cosr;
		double xyc=x*y*nc;
		double xzc=x*z*nc;
		double yzc=y*z*nc;
		double zs=z*sinr;
		double ys=y*sinr;
		double xs=x*sinr;
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
	
	public void loadTranslate(double x,double y,double z)
	{
		loadIdentity();
		m[12]=x;
		m[13]=y;
		m[14]=z;
	}
	public void loadScale(double x,double y,double z)
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
	public static Matrix4d multiply(Matrix4d a,Matrix4d b)
	{
		Matrix4d r=new Matrix4d();
		for(int i=0;i<4;i++)
		{
			double i0=0,i1=0,i2=0,i3=0;
			for(int j=0;j<4;j++)
			{
				double aij=a.m[i*4+j];
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
	public void multiply(Matrix4d b)
	{
		double [] r=new double[16];
		for(int i=0;i<4;i++)
		{
			double i0=0,i1=0,i2=0,i3=0;
			for(int j=0;j<4;j++)
			{
				double aij=m[i*4+j];
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
	private double computeCofactor(int i, int j) {
        int c0 = (i+1) % 4;
        int c1 = (i+2) % 4;
        int c2 = (i+3) % 4;
        int r0 = (j+1) % 4;
        int r1 = (j+2) % 4;
        int r2 = (j+3) % 4;

        double minor = (m[c0 + 4*r0] * (m[c1 + 4*r1] * m[c2 + 4*r2] -
                                            m[c1 + 4*r2] * m[c2 + 4*r1]))
                     - (m[c0 + 4*r1] * (m[c1 + 4*r0] * m[c2 + 4*r2] -
                                            m[c1 + 4*r2] * m[c2 + 4*r0]))
                     + (m[c0 + 4*r2] * (m[c1 + 4*r0] * m[c2 + 4*r1] -
                                            m[c1 + 4*r1] * m[c2 + 4*r0]));

        double cofactor = ((i+j) & 1) != 0 ? -minor : minor;
        return cofactor;
    }
	
	/**
	 * ÇóÄæ
	 * @return ÊÇ·ñÓÐÄæ¾ØÕó
	 */
	public boolean inverse(){
		Matrix4d result = new Matrix4d();

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                result.m[4*i + j] = computeCofactor(i, j);
            }
        }

        // Dot product of 0th column of source and 0th row of result
        double det = m[0]*result.m[0] + m[4]*result.m[1] +
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
	 * Äæ¾ØÕó+×ªÖÃ
	 * @return ·µ»ØÊÇ·ñÓÐÄæ¾ØÕó
	 */
	public boolean inverseTranspose() {

        Matrix4d result = new Matrix4d();

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                result.m[4*j + i] = computeCofactor(i, j);
            }
        }

        double det = m[0]*result.m[0] + m[4]*result.m[4] +
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
	 * ×ªÖÃ
	 */
	public void transpose() {
        for(int i = 0; i < 3; ++i) {
            for(int j = i + 1; j < 4; ++j) {
                double temp = m[i*4 + j];
                m[i*4 + j] = m[j*4 + i];
                m[j*4 + i] = temp;
            }
        }
    }
}
