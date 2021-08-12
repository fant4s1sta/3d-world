package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;



/**
 * For this assignment, we have implemented all the basic components.
 * 
 * For the extension parts, we have implemented:
 * (a). Make the sun move and change color according to the time of day.
 * 		The related codes can be found in Sun.java and also some other classes calling it.
 * (b). Add distance attenuation to the torch light.
 * 		The related codes can be found in our shader: fragment_tex_phong_world.glsl
 * (c). Add ponds with animated textures to your world.
 * 		The related codes can be found in our Pond.java and also some other classes calling it.
 * (d). Fix road extrusion so roads can go up and down hills.
 * 		The related codes can be found in our Road.java.
 * 
 * How to play this world:
 * Use up, down, right, left arrow keys control the camera/avatar.
 * V key: Switch the view.
 * N key: Switch on/off the night mode.
 * T key: Switch on/off the torch (you can only see the torch light in the night mode).
 * S key: Switch on/off the sun mode.
 * 
 * @author Yifan Zhu
 */
public class World extends Application3D {

	private Terrain terrain;
	private Camera camera;
	private Texture texture;

	private boolean nightMode;
	private boolean torchMode;
	private boolean sunMode;

	public World(Terrain terrain) {
		super("Assignment 2", 800, 600);
		this.terrain = terrain;
		this.camera = new Camera(this);
		this.nightMode = false;
		this.torchMode = false;
		this.sunMode = false;
	}

	/**
	 * Load a level file and display it.
	 * @param args - The first argument is a level file in JSON format
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Terrain terrain = LevelIO.load(new File(args[0]));
		World world = new World(terrain);
		world.start();
	}

	public Point3D getAvatarPosition() {
		return camera.getPosition();
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public void switchNightMode() {
		nightMode = !nightMode;
	}

	public void switchTorchMode() {
		torchMode = !torchMode;
	}

	public void switchSunMode() {
		sunMode = !sunMode;
	}

	@Override
	public void display(GL3 gl) {
		int n = this.getNumberOfDisplay();
		super.display(gl);
		terrain.setPondTexture(gl, n);
		Shader.setPenColor(gl, Color.WHITE);
		Shader.setInt(gl, "tex", 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
		Shader.setPoint3D(gl, "directionalLight", new Point3D(terrain.getSunlight().getX(), terrain.getSunlight().getY(), terrain.getSunlight().getZ()));
		Shader.setPoint3D(gl, "torchLight", new Point3D(0, 0, 0));
		if (camera.isThirdPerson()) {
			Shader.setPoint3D(gl, "torchLight", new Point3D(0, 0, -2));
		}
		Shader.setFloat(gl, "cutoff", 30);
		Shader.setColor(gl, "lightIntensity", Color.WHITE);
		Shader.setColor(gl, "ambientCoeff", Color.WHITE);
		Shader.setColor(gl, "ambientIntensityNight", new Color(0.001f, 0.001f, 0.001f));
		Shader.setColor(gl, "ambientIntensityTorch", new Color(0.5f, 0.5f, 0.5f));
		Shader.setColor(gl, "ambientIntensityDay", new Color(0.5f, 0.5f, 0.5f));
		Shader.setColor(gl, "diffuseCoeffNight", new Color(0.1f, 0.1f, 0.1f));
		Shader.setColor(gl, "diffuseCoeffTorch", new Color(0.8f, 0.8f, 0.8f));
		Shader.setColor(gl, "diffuseCoeffDay", new Color(0.5f, 0.5f, 0.5f));
		Shader.setColor(gl, "specularCoeffNight", new Color(0.01f, 0.01f, 0.01f));
		Shader.setColor(gl, "specularCoeffTorch", new Color(1, 1, 1));
		Shader.setColor(gl, "specularCoeffDay", new Color(0.2f, 0.2f, 0.2f));
		Shader.setFloat(gl, "phongExp", 16f);
		Shader.setFloat(gl, "constant", 1.0f);
		Shader.setFloat(gl, "linear", 0.09f);
		Shader.setFloat(gl, "quadratic", 0.032f);
		// Set the shader based on the mode
		if (nightMode && !torchMode) {
			this.setBackground(new Color(0, 0, 0.1f));
			Shader.setInt(gl, "mode", 1);
			Shader.setColor(gl, "lightIntensity", new Color(0.5f, 0.5f, 0.5f));
		} else if (nightMode && torchMode) {
			this.setBackground(new Color(0, 0, 0.1f));
			Shader.setInt(gl, "mode", 2);
			Shader.setColor(gl, "lightIntensity", new Color(0.5f, 0.5f, 0.5f));
		} else {
			this.setBackground(new Color(0.2f, 0.6f, 1.0f));
			Shader.setInt(gl, "mode", 0);
			if (sunMode) {
				camera.moveSun();
				this.setBackground(camera.getSkyColor());
				Shader.setColor(gl, "lightIntensity", camera.getSunColor());
				Shader.setPoint3D(gl, "directionalLight", camera.getSunPosition());
				Shader.setColor(gl, "ambientIntensityDay", camera.getAmbientLight());
			}
		}
		camera.setView(gl);
		CoordFrame3D frame = CoordFrame3D.identity();
		terrain.draw(gl, frame);
		// Keep the avatar not be illuminated by the torch
		if (nightMode && torchMode) {
			Shader.setInt(gl, "mode", 1);
			Shader.setColor(gl, "lightIntensity", new Color(0.5f, 0.5f, 0.5f));
		}
		camera.draw(gl, frame);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
	}

	@Override
	public void init(GL3 gl) {
		int n = this.getNumberOfDisplay();
		super.init(gl);
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl", "shaders/fragment_tex_phong_world.glsl");
		shader.use(gl);
		texture = new Texture(gl, "res/textures/grass.jpg", "jpg", true);
		terrain.init(gl, n);
		camera.init(gl);
		getWindow().addKeyListener(camera);
	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
		super.reshape(gl, width, height);
		Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
	}
}
