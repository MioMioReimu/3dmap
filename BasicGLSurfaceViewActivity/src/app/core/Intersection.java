package app.core;
public class Intersection {
	/**边界或者点重合不计入相交的情况*/
	public static boolean AABB2DwithAABB2D(AABB2D a,AABB2D b) {
		return (Math.abs(a.x-b.x)-a.halfx-a.halfx)<0&&
				(Math.abs(a.y-b.y)-a.halfy-a.halfy)<0;
	}
}
