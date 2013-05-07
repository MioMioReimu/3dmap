package app.core;

public class AABB2D {
	public float x;
	public float y;
	public float halfx;
	public float halfy;
	public AABB2D(float x,float y,float halfX,float halfY){
		this.x=x;this.y=y;this.halfx=halfX;
		this.halfy=halfY;
	}
	public AABB2D(Float2 pos,Float2 halfLength){
		this.x=pos.x;this.y=pos.y;
		this.halfx=halfLength.x;this.halfy=halfLength.y;
	}
	public void setPos(float x,float y) {
		this.x=x;
		this.y=y;
	}
}
