package unsw.graphics.world;

import java.awt.Color;
import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

public class Avatar {

	private TriangleMesh mesh;
	private Texture texture;
	
	private Point3D position;
	private float angleY;

	public Avatar() {
		position = new Point3D(0, 0, 15);
		try {
			mesh = new TriangleMesh("res/models/wolf.ply", true, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Point3D getPosition() {
		return position;
	}
	
	/**
	 * Rotate the avatar.
	 * @param angle
	 */
	public void rotate(float angle) {
		this.angleY = angle;
	}
	
	/**
	 * Set the altitude of the avatar.
	 * @param y
	 */
	public void setY(float y) {
		position = new Point3D(position.getX(), y, position.getZ());
	}

	/**
	 * Make the avatar move forward a unit.
	 */
	public void forward() {
		float x = (float) (position.getX()-Math.sin(Math.toRadians(angleY)));
		float z = (float) (position.getZ()-Math.cos(Math.toRadians(angleY)));
		this.position = new Point3D(x, position.getY(), z);
	}

	/**
	 * Make the avatar move backward a unit.
	 */
	public void backward() {
		float x = (float) (position.getX()+Math.sin(Math.toRadians(angleY)));
		float z = (float) (position.getZ()+Math.cos(Math.toRadians(angleY)));
		this.position = new Point3D(x, position.getY(), z);
	}
	
	public void init(GL3 gl) {
		texture = new Texture(gl, "res/textures/wolf.jpg", "jpg", true);
		mesh.init(gl);
	}
	
	public void draw(GL3 gl, CoordFrame3D frame) {
		Shader.setPenColor(gl, Color.WHITE);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
		frame = frame.rotateX(-90).rotateZ(180).scale(0.8f, 0.8f, 0.8f);
		mesh.draw(gl, frame);
	}

}