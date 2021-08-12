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

public class Pond {

	private List<Point2D> points;
	private Terrain terrain;
	private Texture texture;
	private TriangleMesh mesh;

	public Pond(List<Point2D> corner, Terrain terrain) {
		this.points = corner;
		this.terrain = terrain;
	}
	
	/**
	 * Set the texture of the pond based on the frame number.
	 * @param gl
	 * @param n frame number
	 */
	public void setTexture(GL3 gl, int n) {
		int r = n % 120 + 1;
		String filePath = "res/textures/water/water_" + Integer.toString(r) + ".jpg";
		texture = new Texture(gl, filePath, "jpg", true);
	}

	/**
	 * Make the mesh of the pond.
	 * @return mesh
	 */
	public TriangleMesh makeMesh() {
		List<Point3D> verticesList = new ArrayList<Point3D>();
		List<Point2D> texList = new ArrayList<Point2D>();
		Point2D leftTop = points.get(0);
		Point2D leftBottom = points.get(1);
		Point2D rightBottom = points.get(2);
		Point2D rightTop = points.get(3);
		float altitude = terrain.altitude(leftTop.getX(), leftTop.getY());
		
		verticesList.add(new Point3D(leftTop.getX(), altitude, leftTop.getY()));
		verticesList.add(new Point3D(leftBottom.getX(), altitude, leftBottom.getY()));
		verticesList.add(new Point3D(rightTop.getX(), altitude, rightTop.getY()));
		
		verticesList.add(new Point3D(rightTop.getX(), altitude, rightTop.getY()));
		verticesList.add(new Point3D(leftBottom.getX(), altitude, leftBottom.getY()));
		verticesList.add(new Point3D(rightBottom.getX(), altitude, rightBottom.getY()));
		
		texList.add(new Point2D(leftTop.getX(), leftTop.getY()));
		texList.add(new Point2D(leftBottom.getX(), leftBottom.getY()));
		texList.add(new Point2D(rightTop.getX(), rightTop.getY()));
		
		texList.add(new Point2D(rightTop.getX(), rightTop.getY()));
		texList.add(new Point2D(leftBottom.getX(), leftBottom.getY()));
		texList.add(new Point2D(rightBottom.getX(), rightBottom.getY()));
		
		TriangleMesh mesh = new TriangleMesh(verticesList, true, texList);
		return mesh;
	}

	public void init(GL3 gl, int n) {
		setTexture(gl, n);
		mesh = this.makeMesh();
		mesh.init(gl);
	}

	public void draw(GL3 gl, CoordFrame3D frame) {
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
