import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JPanel;

public class RocketPanel extends JPanel {

	static final int WIDTH = 1600;
	static final int HEIGHT = 800;
	
	public static final double GRAVITATIONAL_CONSTANT = 2;
	public static final double RESTITUTION = 1.1;
	public static final double AIR_FRICTION = 1;

	public static final double THRUST_DELTA = 0.13;
	public static final double THROTTLE_DELTA = 4;
	public static final double ROTATION_SPEED = 0.004;
	public static final double SCALING_RATE = 1.1;
	public static final double MAXIMUM_ZOOM = 0.1;
	public static final double ANGULAR_DECAY = 1.1;
	public static final double CAMERA_ROTATION_SPEED_WEIGHT = 70;
	public static final int INDICATOR_RADIUS = 10;
	public static final double PLANET_DENSITY = 0.4;
	
	private boolean[] KEY_STATE = new boolean[256];
	
	public double scale = 1;
	public double thrust = 0;
	
	public static Simulation sim;

	public double previousCameraRotation = 0;
	
	public RocketPanel() {
		this.setFocusable(true);
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setBackground(Color.WHITE);

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				KEY_STATE[event.getKeyCode()] = true;
			}

			@Override
			public void keyReleased(KeyEvent event) {
				KEY_STATE[event.getKeyCode()] = false;
			}
		});

		Player player = new Player(new Vector2D(0, 0), new Vector2D(0, 0), 10);
		sim = new Simulation(player, new ArrayList<Planet>());
		sim.planets.add(new Planet(new Vector2D(500, 500), 100, Color.RED));
		sim.planets.add(new Planet(new Vector2D(-500, -500), 100, Color.BLUE));
		sim.planets.add(new Planet(new Vector2D(-500, 500), 100, Color.GREEN));
		sim.planets.add(new Planet(new Vector2D(500, -500), 100, Color.YELLOW));
//		sim.planets.add(new Planet(new Vector2D(700, 700), 100, Color.RED));
//		sim.planets.add(new Planet(new Vector2D(-700, -700), 100, Color.BLUE));
//		sim.planets.add(new Planet(new Vector2D(-700, 700), 100, Color.GREEN));
//		sim.planets.add(new Planet(new Vector2D(700, -700), 100, Color.YELLOW));
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						tick();
						Thread.sleep(15);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void tick() {
		Vector2D gravity = sim.player.gravityVector(sim.planets);
		sim.player.vel = sim.player.vel.add(gravity);
		for (Planet planet : sim.planets) {
			if (planet.loc.distanceSq(sim.player.loc) < (planet.radius + sim.player.radius) * (planet.radius + sim.player.radius)) {
				Vector2D relative = new Vector2D(planet.loc.x - sim.player.loc.x, planet.loc.y - sim.player.loc.y);
				relative.setR(sim.player.vel.getR() / RESTITUTION);
				sim.player.vel = relative.scalarMult(-1);
				break;
			}
			planet.tick();
		}
		sim.player.loc = sim.player.loc.add(sim.player.vel);
//		sim.player.vel = sim.player.vel.scalarMult(1 / AIR_FRICTION);
		sim.player.angle += sim.player.angularVel;
		sim.player.angularVel /= ANGULAR_DECAY;
		if (KEY_STATE[KeyEvent.VK_SHIFT]) {
			if(thrust <= 100 - THROTTLE_DELTA)
				thrust += THROTTLE_DELTA;
		}
		if(KEY_STATE[KeyEvent.VK_CONTROL]){
			if(thrust >= THROTTLE_DELTA)
				thrust -= THROTTLE_DELTA;
		}
		if(KEY_STATE[KeyEvent.VK_X]){
			thrust = 0;
		}
		if (KEY_STATE[KeyEvent.VK_A]) {
			sim.player.angularVel -= ROTATION_SPEED;
		}
		if (KEY_STATE[KeyEvent.VK_D]) {
			sim.player.angularVel += ROTATION_SPEED;
		}
		if(KEY_STATE[KeyEvent.VK_UP]){
			if(scale >= MAXIMUM_ZOOM)
				scale /= SCALING_RATE;
		}
		if(KEY_STATE[KeyEvent.VK_DOWN]){
			scale *= SCALING_RATE;
		}
		if(thrust > 0){
			Vector2D v = new Vector2D(THRUST_DELTA * (thrust / 100), THRUST_DELTA * (thrust / 100));
			v.setTheta(sim.player.angle);
			sim.player.vel = sim.player.vel.add(v);
			sim.player.thrustParticles((int) (thrust / 4));
		}
		sim.player.pruneDecayedParticles();
//		System.out.println(sim.player.calculateEccentricity());
		repaint();
	}

	public void render(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Planet sphereInfluence = sim.player.getSOI(sim.planets);
		int dist = (int) sphereInfluence.loc.distance(sim.player.loc) - sphereInfluence.radius;
		int color = Math.min(Math.max(1000 - dist / 5, 0), 255);
		Color backc = new Color(color, color, color);
		g.setColor(backc);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		Vector2D dif = sphereInfluence.loc.subtract(sim.player.loc);
		double ang = -dif.getTheta() + Math.PI / 2;
		double avgAng = (previousCameraRotation * (CAMERA_ROTATION_SPEED_WEIGHT - 1) + ang) / CAMERA_ROTATION_SPEED_WEIGHT;
		
		AffineTransform orig = g.getTransform();
		AffineTransform at = g.getTransform();
		at.scale(scale, scale);
		g.setTransform(at);
		g.translate(-(int) (sim.player.loc.x) + (int)(WIDTH / (2 * scale)), -(int) (sim.player.loc.y) + (int)(HEIGHT / (2 * scale)));
		g.rotate(avgAng, sim.player.loc.x, sim.player.loc.y);
		previousCameraRotation = avgAng;
		
		g.setColor(Color.PINK);
		g.fillOval((int)(sim.player.loc.x - INDICATOR_RADIUS / scale), (int)(sim.player.loc.y - INDICATOR_RADIUS / scale), 
				(int)(2 * INDICATOR_RADIUS / scale), (int)(2 * INDICATOR_RADIUS / scale));
		g.setColor(Color.GRAY);
		g.drawLine((int)(sim.player.loc.x), (int)(sim.player.loc.y), (int)(sim.player.loc.x + dif.x), (int)(sim.player.loc.y + dif.y));
		for (Planet planet : sim.planets)
			planet.draw(g);
		sim.player.draw(g);
		
		g.setTransform(orig);
		g.setColor(Color.GRAY);
		String[] text = { "Speed: " + (int) sim.player.vel.length(),
						"Altitude: " + dist,
						"Thrust: " + thrust,
						"Player: " + sim.player.toString(),
						"Particle Count: " + sim.player.particles.size(),
						"SOI: " + sphereInfluence.toString(),
						"Camera Rotation: " + Math.round(Math.toDegrees(avgAng) * 1000) / 1000.0,
						"Camera Scale: " + Math.round(scale * 1000) / 1000.0
						};
		for (int i = 0; i < text.length; i++) {
			g.drawString(text[i], 10, i * 15 + 15);
		}
	}

	@Override
	public void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = (Graphics2D) gr;
		render(g);
	}
}
