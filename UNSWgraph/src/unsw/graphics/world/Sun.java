package unsw.graphics.world;

import java.awt.Color;

public class Sun {

	private float angleY;
	private float x;
	private float y;
	private float z;
	private Color sunColor;
	private Color ambientColor;
	private Color skyColor;
	private static final Color daySky = new Color(0.2f, 0.6f, 1.0f);
	private static final Color nightSky = new Color(0f, 0f, 0.1f);

	public Sun() {
		reset();
	}
	
	/**
	 * Reset the sun.
	 */
	public void reset() {
		this.angleY = 0;
		this.x = (float) Math.cos(Math.toRadians(angleY));
		this.y = (float) Math.sin(Math.toRadians(angleY));
		this.z = 0;
		this.sunColor = new Color(0f, 0f, 0f);
		this.ambientColor = new Color(0.5f, 0.5f, 0.5f);
		this.skyColor = new Color(0.2f, 0.6f, 1.0f);
	}

	/**
	 * Make the sun move with changing the color.
	 */
	public void move() {
		if (this.angleY == 360) {
			this.angleY = 0;
		}
		if (this.angleY <= 179.5) {
			this.angleY += 0.5f;
			float diff = Math.abs(90-angleY);
			this.sunColor = new Color(1.0f, (float) (1.0-diff/90), (float) (1.0-diff/90));
			this.ambientColor = new Color(0.5f, 0.5f, 0.5f);
			this.skyColor = daySky;
		} else {
			this.angleY += 0.5f;
			this.sunColor = new Color(0f, 0f, 0f);
			this.ambientColor = new Color(0f, 0f, 0.001f);
			this.skyColor = nightSky;
		}
		this.x = (float) Math.cos(Math.toRadians(angleY));
		this.y = (float) Math.sin(Math.toRadians(angleY));
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public float getAngle() {
		return angleY;
	}

	public Color getSunColor() {
		return sunColor;
	}

	public void setSunColor(Color sunColor) {
		this.sunColor = sunColor;
	}

	public Color getAmbientLight() {
		return ambientColor;
	}

	public Color getSkyColor() {
		return skyColor;
	}

	public void setSkyColor(Color skyColor) {
		this.skyColor = skyColor;
	}

}