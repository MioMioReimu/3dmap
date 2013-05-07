package app.resource;

import java.util.ArrayList;

public abstract class Drawable {
	String id;
	ArrayList<Mesh> meshes;
	
	public String  getid()
	{
		return id;
	}
	public Drawable(String id)
	{
		this.id=id;
		this.meshes=new ArrayList<Mesh>();
	}
	
	public void addMesh(Mesh m)
	{
		this.meshes.add(m);
	}
}
