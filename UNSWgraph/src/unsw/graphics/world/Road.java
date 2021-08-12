package unsw.graphics.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

	private List<Point2D> points;
	private float width;
	private Terrain terrain;
	private Texture texture;
	private TriangleMesh mesh;

	/**
	 * Create a new road with the specified spine 
	 * @param width
	 * @param spine
	 */
	public Road(float width, List<Point2D> spine, Terrain terrain) {
		this.width = width;
		this.points = spine;
		this.terrain = terrain;
	}

	/**
	 * The width of the road.
	 * @return width
	 */
	public double width() {
		return width;
	}

	/**
	 * Get the number of segments in the curve
	 * @return size
	 */
	public float size() {
		return points.size() / 3;
	}

	/**
	 * Get the specified control point.
	 * @param i
	 * @return point
	 */
	public Point2D controlPoint(int i) {
		return points.get(i);
	}

	/**
	 * Get a point on the spine. The parameter t may vary from 0 to size().
	 * Points on the kth segment take have parameters in the range (k, k+1).
	 * @param t
	 * @return point
	 */
	public Point3D point(float t) {
		int i = (int)Math.floor(t);
		t = t - i;

		i *= 3;

		Point2D p0 = points.get(i++);
		Point2D p1 = points.get(i++);
		Point2D p2 = points.get(i++);
		Point2D p3 = points.get(i++);


		float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
		float z = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();
		float y = terrain.altitude(x, z);

		return new Point3D(x, y, z);
	}

	/**
	 * Get the tangent vector.
	 * @param t
	 * @return tangent vector
	 */
	public Point3D tangent(float t) {
		int i = (int)Math.floor(t);
		t = t - i;

		i *= 3;

		Point2D p0 = points.get(i++);
		Point2D p1 = points.get(i++);
		Point2D p2 = points.get(i++);
		Point2D p3 = points.get(i++);

		float x = bTangent(0, t) * (p1.getX() - p0.getX()) + bTangent(1, t) * (p2.getX() - p1.getX()) + bTangent(2, t) * (p3.getX() - p2.getX());
		float z = bTangent(0, t) * (p1.getY() - p0.getY()) + bTangent(1, t) * (p2.getY() - p1.getY()) + bTangent(2, t) * (p3.getY() - p2.getY());
		float y = 0;

		return new Point3D(x, y, z);
	}

	/**
	 * Calculate the Bezier coefficients.
	 * @param i
	 * @param t
	 * @return coefficient
	 */
	private float b(int i, float t) {

		switch(i) {

		case 0:
			return (1-t) * (1-t) * (1-t);

		case 1:
			return 3 * (1-t) * (1-t) * t;

		case 2:
			return 3 * (1-t) * t * t;

		case 3:
			return t * t * t;
		}

		// this should never happen
		throw new IllegalArgumentException("" + i);
	}

	/**
	 * Calculate the Bezier tangent coefficients.
	 * @param i
	 * @param t
	 * @return coefficient
	 */
	private float bTangent(int i, float t) {

		switch(i) {

		case 0:
			return (1-t) * (1-t);

		case 1:
			return 2 * (1-t) * t;

		case 2:
			return t * t;
		}

		// this should never happen
		throw new IllegalArgumentException("" + i);
	}

	/**
	 * Get the normal vector based on the given tangent vector.
	 * @param tangent
	 * @return normal vector
	 */
	public Point3D normal(Point3D tangent) {
		float x = -tangent.getZ();
		float z = tangent.getX();
		float y = 0;
		float mod = (float) Math.sqrt(tangent.getX() * tangent.getX() + tangent.getZ() * tangent.getZ());
		x = x / mod * (width / 2);
		z = z / mod * (width / 2);
		return new Point3D(x, y, z);
	}

	/**
	 * Make the mesh of the road.
	 * @return mesh
	 */
	public TriangleMesh makeMesh() {
		List<Point3D> verticesList = new ArrayList<Point3D>();
		List<Point2D> texList = new ArrayList<Point2D>();
		for (float t = 0; t < this.size()-0.002; t += 0.002) {
			Point3D currentSpine = point(t);
			Point3D nextSpine = point((float) (t+0.002));
			Point3D currentTangent = tangent(t);
			Point3D nextTangent = tangent((float) (t+0.002));
			Point3D currentNormal = normal(currentTangent);
			Point3D nextNormal = normal(nextTangent);
			Point3D currentLeft = new Point3D(currentSpine.getX()-currentNormal.getX(), currentSpine.getY(), currentSpine.getZ()-currentNormal.getZ());
			Point3D currentRight = new Point3D(currentSpine.getX()+currentNormal.getX(), currentSpine.getY(), currentSpine.getZ()+currentNormal.getZ());
			Point3D nextLeft = new Point3D(nextSpine.getX()-nextNormal.getX(), nextSpine.getY(), nextSpine.getZ()-nextNormal.getZ());
			Point3D nextRight = new Point3D(nextSpine.getX()+nextNormal.getX(), nextSpine.getY(), nextSpine.getZ()+nextNormal.getZ());

			verticesList.add(currentLeft);
			texList.add(new Point2D(currentLeft.getX(), currentLeft.getZ()));
			verticesList.add(currentRight);
			texList.add(new Point2D(currentRight.getX(), currentRight.getZ()));
			verticesList.add(nextLeft);
			texList.add(new Point2D(nextLeft.getX(), nextLeft.getZ()));

			verticesList.add(nextLeft);
			texList.add(new Point2D(nextLeft.getX(), nextLeft.getZ()));
			verticesList.add(currentRight);
			texList.add(new Point2D(currentRight.getX(), currentRight.getZ()));
			verticesList.add(nextRight);
			texList.add(new Point2D(nextRight.getX(), nextRight.getZ()));
		}
		TriangleMesh mesh = new TriangleMesh(verticesList, true, texList);
		return mesh;
	}

	public void init(GL3 gl) {
		texture = new Texture(gl, "res/textures/rock.bmp", "bmp", true);
		mesh = this.makeMesh();
		mesh.init(gl);
	}

	public void draw(GL3 gl, CoordFrame3D frame) {
		// Prevent the z fighting
		gl.glEnable(GL3.GL_POLYGON_OFFSET_POINT);
		gl.glEnable(GL3.GL_POLYGON_OFFSET_LINE);
		gl.glEnable(GL3.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(-1.0f, -1.0f);
		Shader.setPenColor(gl, Color.WHITE);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
		mesh.draw(gl, frame);
		gl.glPolygonOffset(0.0f, 0.0f);
		gl.glDisable(GL3.GL_POLYGON_OFFSET_POINT);
		gl.glDisable(GL3.GL_POLYGON_OFFSET_LINE);
		gl.glDisable(GL3.GL_POLYGON_OFFSET_FILL);
	}

}
