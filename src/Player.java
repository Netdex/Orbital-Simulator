import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;


public class Player extends Entity {
	
	public static final int PARTICLE_DECAY = 20;
	
	public int radius;
	public double angle;
	public double angularVel;
	
	public volatile ArrayList<Particle> particles = new ArrayList<Particle>();
	
	public Player(Vector2D loc, Vector2D vel, int radius){
		this.loc = loc;
		this.vel = vel;
		this.radius = radius;
	}
	public Player(int radius){
		this.loc = new Vector2D(100, 100);
		this.vel = new Vector2D(0, 0);
		this.radius = radius;
	}
	
	public int getAltitude(){
		return -(int)(loc.y - RocketPanel.HEIGHT);
	}
	
	public void thrustParticles(int amt){
		for(int i = 0; i < amt; i++){
			Vector2D particleSlope = new Vector2D(5 + Math.random() * 15 - 7.5, 5 + Math.random() * 15 - 7.5);
			particleSlope.setTheta(angle - Math.PI - (Math.random() * 1) + 0.5);
			int red = (int)(Math.random() * 56) + 200;
			Color c = new Color(red, red - (int)(Math.random() * 200), 0);
			particles.add(new Particle(new Vector2D(loc), particleSlope, c, PARTICLE_DECAY, (int)(Math.random() * 5 + 2)));
		}
	}
	
	public void pruneDecayedParticles(){
		for(int i = 0; i < particles.size(); i++){
			Particle p = particles.get(i);
			boolean life = p.tick();
			if(!life){
				particles.remove(i);
				i--;
			}
		}
	}
	
	@Override
	public Vector2D gravityVector(ArrayList<Planet> planets){
		return gravityVector(loc, planets);
	}
	
	public Vector2D getAngularMomentum(Planet soi){
		return soi.loc.subtract(loc).mult(vel);
	}
	
	public double calculateEccentricity(){
		Vector2D r = loc.subtract(this.getSOI(RocketPanel.sim.planets).loc);
		Vector2D h = r.mult(vel);
		double gravstd = RocketPanel.GRAVITATIONAL_CONSTANT;
		Vector2D e = vel.mult(h).scalarDiv(gravstd).subtract(r.scalarDiv(r.length()));
//		System.out.println(e);
		return e.length();
		
	}
	
	@Override
	public void draw(Graphics2D g){
		g.setStroke(new BasicStroke(5));
		// Calculate angle of ship
		Vector2D v = new Vector2D(10, 10);
		v.setTheta(angle);
		g.setColor(Color.RED);
		// Draw ship
		g.drawLine((int)loc.x, (int)loc.y, (int)(loc.x + v.x), (int)(loc.y + v.y));
		g.setStroke(new BasicStroke(1));
		// Draw particles
		for(int i = 0; i < particles.size(); i++){
			Particle p = particles.get(i);
			p.draw(g);
		}
		Player player = RocketPanel.sim.player;
		
		// Draw trajectory
		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(5));
		
		Vector2D loc = new Vector2D(RocketPanel.sim.player.loc);
		Vector2D vel = new Vector2D(RocketPanel.sim.player.vel);
		Vector2D prevPoint = new Vector2D(loc);
		
		// Keep track of apoapsis and periapsis
		Vector2D apoapsis = new Vector2D(loc);
		Vector2D periapsis = new Vector2D(loc);
		Planet soi = getSOI(RocketPanel.sim.planets);
		for(int i = 0; i < 10000; i++){
			// Run gravity simulation
			Vector2D gravity = gravityVector(loc, RocketPanel.sim.planets);
			vel = vel.add(gravity);
			loc = loc.add(vel);
			if(prevPoint != null){
				g.draw(new Line2D.Double(prevPoint, loc));
			}
			prevPoint.set(loc);
			// Check for collision
			for(Planet planet : RocketPanel.sim.planets){
				if (planet.loc.distanceSq(loc) < (planet.radius + player.radius) * (planet.radius + player.radius)) {
					i = 9999999;
					break;
				}
				if(loc.distanceSq(RocketPanel.sim.player.loc) < 25 && i > 10){
					i = 9999999;
					break;
				}
				// Check for change in periapsis or apoapsis
				double dist = loc.distanceSq(planet.loc);
				if(planet == soi){
					if(dist > apoapsis.distanceSq(soi.loc)){
						apoapsis.set(loc);
					}
					else if(dist < periapsis.distanceSq(soi.loc)){
						periapsis.set(loc);
					}
				}
			}
		}
		g.setColor(Color.BLUE);
		g.drawString("Apoapsis", (int)(apoapsis.x), (int)(apoapsis.y));
		g.draw(new Line2D.Double(apoapsis, apoapsis));
		g.drawString("Periapsis", (int)(periapsis.x), (int)(periapsis.y));
		g.draw(new Line2D.Double(periapsis, periapsis));
		double ra = apoapsis.distance(soi.loc);
		double rp = periapsis.distance(soi.loc);
		double e = (ra - rp)/(ra + rp);
//		System.out.println(e);
		
		g.setStroke(new BasicStroke(1));
	}
	
	@Override
	public String toString(){
		return String.format("{loc: %s, vel: %s, angle: %f, angleVel: %f}", loc.toString(), vel.toString(), 
				Math.round(angle * 1000) / 1000.0, Math.round(angle * 1000) / 1000.0);
	}
}
