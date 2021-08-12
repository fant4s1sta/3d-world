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

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

	private Point3D position;
	private TriangleMesh mesh;
	private Texture texture;

	public Tree(float x, float y, float z) {
		position = new Point3D(x, y, z);
		try {
			mesh = new TriangleMesh("res/models/tree.ply", true, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Point3D getPosition() {
		return position;
	}

	public void init(GL3 gl) {
		texture = new Texture(gl, "res/textures/trunk.jpg", "jpg", true);
		mesh.init(gl);
	}

	public void draw(GL3 gl, CoordFrame3D frame) {
		Shader.setPenColor(gl, Color.WHITE);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
		frame = frame.translate(position.getX(), position.getY()+0.5f, position.getZ()).rotateY(-45).scale(0.1f, 0.1f, 0.1f);
		mesh.draw(gl, frame);
	}

}
