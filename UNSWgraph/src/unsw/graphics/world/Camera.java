package unsw.graphics.world;

import java.awt.Color;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point3D;

public class Camera implements KeyListener {

	private World world;
	private Terrain terrain;
	private Avatar avatar;
	private Sun sun;
	
	private boolean thirdPerson;

	private Point3D position;
	private float angleX;
	private float angleY;
	private float angleZ;
	private float scale;

	public Camera(World world) {
		this.world = world;
		this.terrain = world.getTerrain();
		this.position = new Point3D(0, 1, 15);
		this.angleX = 0;
		this.angleY = 0;
		this.angleZ = 0;
		this.scale = 1;
		this.avatar = new Avatar();
		this.thirdPerson = false;
		this.sun = new Sun();
	}
	
	public Point3D getPosition() {
		return avatar.getPosition();
	}

	public Point3D getSunPosition() {
		return new Point3D(sun.getX(), sun.getY(), sun.getZ());
	}

	public float getSunAngle() {
		return sun.getAngle();
	}

	public Color getSunColor() {
		return sun.getSunColor();
	}

	public Color getAmbientLight() {
		return sun.getAmbientLight();
	}

	public Color getSkyColor() {
		return sun.getSkyColor();
	}

	public void moveSun() {
		sun.move();
	}

	public boolean isThirdPerson() {
		return thirdPerson;
	}

	public void setView(GL3 gl) {
		CoordFrame3D viewFrame = CoordFrame3D.identity()
				.scale(1/scale, 1/scale, 1/scale)
				.rotateX(-angleX).rotateY(-angleY).rotateZ(-angleZ)
				.translate(-position.getX(), -position.getY(), -position.getZ());
		Shader.setViewMatrix(gl, viewFrame.getMatrix());
	}

	public void init(GL3 gl) {
		avatar.init(gl);
	}

	public void draw(GL3 gl, CoordFrame3D frame) {
		if (thirdPerson) {
			frame = frame.translate(avatar.getPosition().getX(), avatar.getPosition().getY(), avatar.getPosition().getZ()).rotateY(angleY);
			avatar.draw(gl, frame);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		float x, y, z;
		switch(e.getKeyCode()) {
		case KeyEvent.VK_N:
			world.switchNightMode();
			break;
			
		case KeyEvent.VK_T:
			world.switchTorchMode();
			break;
			
		case KeyEvent.VK_S:
			world.switchSunMode();
			sun.reset();
			break;

		case KeyEvent.VK_V:
			if (thirdPerson) {
				x = (float) (position.getX()-2*Math.sin(Math.toRadians(angleY)));
				z = (float) (position.getZ()-2*Math.cos(Math.toRadians(angleY)));
				position = new Point3D(x, position.getY(), z);
				thirdPerson = false;
			} else {
				x = (float) (position.getX()+2*Math.sin(Math.toRadians(angleY)));
				z = (float) (position.getZ()+2*Math.cos(Math.toRadians(angleY)));
				position = new Point3D(x, position.getY(), z);
				thirdPerson = true;;
			}
			break;

		case KeyEvent.VK_LEFT:
			angleY += 5;
			angleY = MathUtil.normaliseAngle(angleY);
			avatar.rotate(angleY);
			if (thirdPerson) {
				x = (float) (avatar.getPosition().getX()+2*Math.sin(Math.toRadians(angleY)));
				z = (float) (avatar.getPosition().getZ()+2*Math.cos(Math.toRadians(angleY)));
				this.position = new Point3D(x, position.getY(), z);
			}
			break;

		case KeyEvent.VK_RIGHT:
			angleY -= 5;
			angleY = MathUtil.normaliseAngle(angleY);
			avatar.rotate(angleY);
			if (thirdPerson) {
				x = (float) (avatar.getPosition().getX()+2*Math.sin(Math.toRadians(angleY)));
				z = (float) (avatar.getPosition().getZ()+2*Math.cos(Math.toRadians(angleY)));
				this.position = new Point3D(x, position.getY(), z);
			}
			break;

		case KeyEvent.VK_UP:
			x = (float) (position.getX()-Math.sin(Math.toRadians(angleY)));
			z = (float) (position.getZ()-Math.cos(Math.toRadians(angleY)));
			y = 1;
			avatar.forward();
			if (this.terrain.isOnTerrain(avatar.getPosition().getX(), avatar.getPosition().getZ())) {
				avatar.setY(this.terrain.altitude(avatar.getPosition().getX(), avatar.getPosition().getZ()));
			} else {
				avatar.setY(0);
			}
			if (thirdPerson) {
				y = avatar.getPosition().getY() + 1;
			} else {
				if (this.terrain.isOnTerrain(x, z)) {
					y = this.terrain.altitude(x, z) + 1;
				}
			}
			position = new Point3D(x, y, z);
			break;

		case KeyEvent.VK_DOWN:
			x = (float) (position.getX()+Math.sin(Math.toRadians(angleY)));
			z = (float) (position.getZ()+Math.cos(Math.toRadians(angleY)));
			y = 1;
			avatar.backward();
			if (this.terrain.isOnTerrain(avatar.getPosition().getX(), avatar.getPosition().getZ())) {
				avatar.setY(this.terrain.altitude(avatar.getPosition().getX(), avatar.getPosition().getZ()));
			} else {
				avatar.setY(0);
			}
			if (thirdPerson) {
				y = avatar.getPosition().getY() + 1;
			} else {
				if (this.terrain.isOnTerrain(x, z)) {
					y = this.terrain.altitude(x, z) + 1;
				}
			}
			position = new Point3D(x, y, z);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}