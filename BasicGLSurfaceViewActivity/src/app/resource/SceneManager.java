package app.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import app.core.AABB2D;
import app.core.Float3;
import app.core.Int2;
import app.core.Int3;
import app.core.Intersection;
import app.core.Matrix4f;

public class SceneManager implements Renderer{
	PersCamera camera;
	HashMap<Long, Block>blockList;
	Int3 referPoint;
	/**采用双缓存机制,防止在渲染线程和更新线程出现冲突*/
	ArrayList<Block>potentialBlocks;
	ArrayList<Block>tmppotentialBlocks;
	AABB2D visibleRegion =new AABB2D(0,0,4500,4500);
	AABB2D CachedRegion =new AABB2D(0,0,50000,50000);
	private static String blockpath="/sdcard/map/blocks/";
	//ArrayList<Mesh>renderQueue;
	private MaterialLib matlib;
	private ShaderLib shaderlib;
	private Mesh testMesh;
	private float[] viewMatrix=new float[16];
	private float[] proMatrix=new float[16];
	public SceneManager(){
		Int2 xzid=Block.calculatexzid(14160203733L);
		referPoint=new Int3((int)Block.calculatexybyid(xzid.x),0,(int)Block.calculatexybyid(xzid.y));
		this.camera=new PersCamera();
		this.camera.lookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
		this.blockList=new HashMap<Long, Block>();
		this.potentialBlocks=new ArrayList<Block>();
		this.tmppotentialBlocks=new ArrayList<Block>();
		this.testMesh=new Mesh(Mesh.TRILIST|Mesh.HASUV, m);
	}
	public void renderInit(){
		this.matlib=new MaterialLib();
		this.shaderlib=new ShaderLib();
		this.matlib.Init(shaderlib);
	}
	
	/**
	 * 删除在缓存范围外的物体
	 */
	public void removeOutBoundBlocks()
	{
		Float3 camPos=camera.getPosition();
		AABB2D region=new AABB2D(camPos.x,camPos.z,CachedRegion.halfx,CachedRegion.halfy);
		Collection<Block>blocks=blockList.values();
		Iterator<Block>it=blocks.iterator();
		while(it.hasNext()){
			Block next=it.next();
			if(!Intersection.AABB2DwithAABB2D(region, next.wvb)) {
				it.remove();
			}
		}
	}
	
	/**
	 * 更新可能可见的区域
	 */
	public void updatePotentialBlocks(){
		tmppotentialBlocks.clear();
		Float3 p=camera.getPosition();
		int x=(int)p.x+referPoint.x;
		int minxid=Block.calculatexidbyPos(x-(int)visibleRegion.halfx);
		int maxxid=Block.calculatexidbyPos(x+(int)visibleRegion.halfx);
		int z=(int)p.z+referPoint.z;
		int minzid=Block.calculatexidbyPos(z-(int)visibleRegion.halfy)+1;
		int maxzid=Block.calculatexidbyPos(z+(int)visibleRegion.halfy)+1;
		for(int i=minxid;i<maxxid+1;i++)
			for(int j=minzid;j<maxzid+1;j++) {
				Long id=Long.valueOf(Block.calculateid(i, j));
				Block b=this.blockList.get(id);
				if(b==null) {
					b=this.loadBlock(id.longValue());
				}
				if(b!=null){
				this.blockList.put(id, b);
				this.tmppotentialBlocks.add(b);
				}
			}
		ArrayList<Block>tmp=potentialBlocks;
		synchronized (potentialBlocks) {
			potentialBlocks=tmppotentialBlocks;
		}
		tmppotentialBlocks=tmp;
	}
	
	public Block loadBlock(long id) {
		String blockPath = blockpath+String.valueOf(id)+".xml";
		Block block=Block.createBlockFromPath(id, blockPath);
		//this.blockList.put(Long.valueOf(id), block);
		if(block!=null)
			block.updateInfoInWorld(referPoint);
		return block;
	}
	
	public void Draw(){
		Iterator<Block>it=this.potentialBlocks.iterator();
		float[] vpMatrix=Matrix4f.multiply( camera.viewMatrix4f,camera.projectionMatrix4f).getArray();
		//float[] vpMatrix=new float[16];
		//android.opengl.Matrix.multiplyMM(vpMatrix, 0, proMatrix, 0, viewMatrix, 0);
		while(it.hasNext()){
			Block i=it.next();
			float[] mMatrix=i.getBlockMatrix().getArray();
			
			Collection<Drawable>elements=i.elements.values();
			Iterator<Drawable>eleIt=elements.iterator();
			while(eleIt.hasNext()){
				ArrayList<Mesh>meshes=eleIt.next().meshes;
				for(int j=0;j<meshes.size();j++){
					meshes.get(j).Render(this.matlib, mMatrix, vpMatrix);
				}
			}
		}
		testMesh.setMaterial(matlib.getMaterial("road"));
		testMesh.Render(matlib, new Matrix4f().getArray(), vpMatrix);
	}
	@Override
	public void onDrawFrame(GL10 gl) {
		 GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
	     GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
	     updatePotentialBlocks();
	     removeOutBoundBlocks();
		 this.Draw();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		this.camera.persective(120, (float)width/(float)height, 100, 2000);
		//android.opengl.Matrix.perspectiveM(proMatrix, 0, 60, (float)width/(float)height, 1000, 200000);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		this.camera.lookAt(0, 600, 0, 0, 0, 0, 1, 0, 0);
		//android.opengl.Matrix.setLookAtM(this.viewMatrix,0, 0, 15000, 0, 0, 0, 0, 1, 0, 0);
		renderInit();
	}
	
	public void loadInBoundBlocks(){
		Float3 campos=this.camera.getPosition();
		int x=this.referPoint.x+(int)campos.x;
		int z=this.referPoint.z+(int)campos.z;
		
	}
	
	private float m[]={-100,0,100,0,1,100,0,100,1,1,100,0,-100,1,0
			-100,0,100,0,1,100,0,-100,1,0,-100,0,-100,0,0,
			100,0,-100,1,0,100,0,100,1,1,-100,0,100,0,1,
			-100,0,-100,0,0,100,0,-100,1,0,-100,0,100,0,1,
			};
}
