package unsw.graphics.world;



import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

	private int width;
	private int depth;
	private float[][] altitudes;
	private List<Tree> trees;
	private List<Road> roads;
	private List<Pond> ponds;
	private Vector3 sunlight;
	private TriangleMesh mesh;

	/**
	 * Create a new terrain.
	 * @param width The number of vertices in the x-direction
	 * @param depth The number of vertices in the z-direction
	 */
	public Terrain(int width, int depth, Vector3 sunlight) {
		this.width = width;
		this.depth = depth;
		altitudes = new float[width][depth];
		trees = new ArrayList<Tree>();
		roads = new ArrayList<Road>();
		ponds = new ArrayList<Pond>();
		this.sunlight = sunlight;
	}

	public int getWidth() {
		return width;
	}

	public int getDepth() {
		return depth;
	}

	public List<Tree> trees() {
		return trees;
	}

	public List<Road> roads() {
		return roads;
	}

	public Vector3 getSunlight() {
		return sunlight;
	}

	/**
	 * Set the sunlight direction. 
	 * Note: the sun should be treated as a directional light, without a position.
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	public void setSunlightDir(float dx, float dy, float dz) {
		sunlight = new Vector3(dx, dy, dz);      
	}

	/**
	 * Get the altitude at a grid point.
	 * @param x
	 * @param z
	 * @return altitude
	 */
	public double getGridAltitude(int x, int z) {
		return altitudes[x][z];
	}

	/**
	 * Set the altitude at a grid point.
	 * @param x
	 * @param z
	 */
	public void setGridAltitude(int x, int z, float h) {
		altitudes[x][z] = h;
	}

	/**
	 * Get the altitude at an arbitrary point. 
	 * Non-integer points should be interpolated from neighbouring grid points.
	 * Reference: https://codeplea.com/triangular-interpolation.
	 * @param x
	 * @param z
	 * @return altitude
	 */
	public float altitude(float x, float z) {
		float altitude = 0;
		if (x < 0 || x > width-1 || z < 0 || z > depth-1) {
			return 0;
		}
		int floorX = (int) Math.floor(x);
		int floorZ = (int) Math.floor(z);
		if (x == floorX && z == floorZ) {
			return altitudes[floorX][floorZ];
		}
		// check which triangle the point is locating in
		float x1,y1,z1;
		if ((x-floorX+z-floorZ) > 1) {
			x1 = floorX+1;
			y1 = altitudes[floorX+1][floorZ+1];
			z1 = floorZ+1;
		} else {
			x1 = floorX;
			y1 = altitudes[floorX][floorZ];
			z1 = floorZ;
		}

		float x2,y2,z2;
		x2 = floorX+1;
		y2 = altitudes[floorX+1][floorZ];
		z2 = floorZ;

		float x3,y3,z3;
		x3 = floorX;
		y3 = altitudes[floorX][floorZ+1];
		z3 = floorZ+1;

		float determinant = (z2-z3)*(x1-x3) + (x3-x2)*(z1-z3);
		float l1 = ((z2-z3)*(x-x3) + (x3-x2)*(z-z3))/determinant;
		float l2 = ((z3-z1)*(x-x3) + (x1-x3)*(z-z3))/determinant;
		float l3 = 1-l1-l2;
		altitude = l1*y1 + l2*y2 + l3*y3;
		return altitude;
	}

	/**
	 * Add a tree at the specified (x,z) point. 
	 * The tree's y coordinate is calculated from the altitude of the terrain at that point.
	 * @param x
	 * @param z
	 */
	public void addTree(float x, float z) {
		float y = altitude(x, z);
		Tree tree = new Tree(x, y, z);
		trees.add(tree);
	}


	/**
	 * Add a road.
	 * @param x
	 * @param z
	 */
	public void addRoad(float width, List<Point2D> spine) {
		Road road = new Road(width, spine, this);
		roads.add(road);        
	}

	public void addPond(List<Point2D> corner) {
		Pond pond = new Pond(corner, this);
		ponds.add(pond);
	}

	/**
	 * Determine whether the input (x, z) is on the terrain.
	 * @param x
	 * @param z
	 * @return true if it is on the terrain, otherwise false.
	 */
	public boolean isOnTerrain(float x, float z) {
		return x >= 0 && z >= 0 && x < width-1 && z < depth-1;
	}

	/**
	 * Get a list of vertices for generating the mesh.
	 * @return list
	 */
	public List<Point3D> getVertices() {
		List<Point3D> list = new ArrayList<Point3D>();
		for (int x = 0; x < width-1; x++) {
			for (int z = 0; z < depth-1; z++) {
				Point3D point1 = new Point3D(x, (float) getGridAltitude(x, z), z);
				Point3D point2 = new Point3D(x, (float) getGridAltitude(x, z+1), z+1);
				Point3D point3 = new Point3D(x+1, (float) getGridAltitude(x+1, z), z);
				list.add(point1);
				list.add(point2);
				list.add(point3);
				Point3D point4 = new Point3D(x+1, (float) getGridAltitude(x+1, z), z);
				Point3D point5 = new Point3D(x, (float) getGridAltitude(x, z+1), z+1);
				Point3D point6 = new Point3D(x+1, (float) getGridAltitude(x+1, z+1), z+1);
				list.add(point4);
				list.add(point5);
				list.add(point6);
			}
		}
		return list;
	}

	/**
	 * Get a list of texture coordinates.
	 * @return list
	 */
	public List<Point2D> getTexCoords() {
		List<Point2D> list = new ArrayList<Point2D>();
		for (int x = 0; x < width-1; x++) {
			for (int z = 0; z < depth-1; z++) {
				Point2D point1 = new Point2D(x, z);
				Point2D point2 = new Point2D(x+1, z);
				Point2D point3 = new Point2D(x+1, z+1);
				list.add(point1);
				list.add(point2);
				list.add(point3);
				Point2D point4 = new Point2D(x+1, z);
				Point2D point5 = new Point2D(x, z+1);
				Point2D point6 = new Point2D(x+1, z+1);
				list.add(point4);
				list.add(point5);
				list.add(point6);
			}
		}
		return list;
	}

	public void init(GL3 gl, int n) {
		List<Point3D> verticesList = getVertices();
		List<Point2D> textList = getTexCoords();
		mesh = new TriangleMesh(verticesList, true, textList);
		mesh.init(gl);
		for (int i = 0; i < this.trees.size(); i++) {
			this.trees.get(i).init(gl);
		}
		for (int i = 0; i < this.roads.size(); i++) {
			this.roads.get(i).init(gl);
		}
		for (int i = 0; i < this.ponds.size(); i++) {
			this.ponds.get(i).init(gl, n);
		}
	}

	public void draw(GL3 gl, CoordFrame3D frame) {
		mesh.draw(gl, frame);
		for (int i = 0; i < this.trees.size(); i++) {
			this.trees.get(i).draw(gl, frame);
		}
		for (int i = 0; i < this.roads.size(); i++) {
			this.roads.get(i).draw(gl, frame);
		}
		for (int i = 0; i < this.ponds.size(); i++) {
			this.ponds.get(i).draw(gl, frame);
		}
	}

	/**
	 * Set the pond texture based on the frame number.
	 * @param gl
	 * @param n frame number
	 */
	public void setPondTexture(GL3 gl, int n) {
		for (int i = 0; i < this.ponds.size(); i++) {
			this.ponds.get(i).setTexture(gl, n);
		}
	}

}