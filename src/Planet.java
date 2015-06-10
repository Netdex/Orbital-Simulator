import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;


public class Planet extends Entity {
	
	public static int lastID = 0;
	
	public int id;
	public int radius;
	private Color color;
	
	public Planet(Vector2D loc, int radius, Color color){
		this.loc = loc;
		this.vel = new Vector2D(0,0);
		this.radius = radius;
		this.color = color;
		this.id = lastID;
		lastID++;
	}
	
	public double getMass(){
		return Math.PI * radius * radius * RocketPanel.PLANET_DENSITY;
	}
	
	public void tick(){
		this.loc = this.loc.add(vel);
		this.vel = this.vel.add(gravityVector(RocketPanel.sim.planets));
		for (Planet planet : RocketPanel.sim.planets) {
			if (planet.loc.distanceSq(loc) < (planet.radius + radius) * (planet.radius + radius)) {
				Vector2D relative = new Vector2D(planet.loc.x - loc.x, planet.loc.y - loc.y);
				relative.setR(vel.getR() / RocketPanel.RESTITUTION);
				vel = relative.scalarMult(-1);
				break;
			}
		}
	}
	
	@Override
	public void draw(Graphics2D g){
		g.setColor(color);
		g.fillOval((int)(loc.x - radius), (int)(loc.y - radius), radius * 2, radius * 2);
	}
	
	@Override
	public Vector2D gravityVector(ArrayList<Planet> planets){
		Vector2D gravity = new Vector2D(0,0);
		for(Planet planet : planets){
			if(planet != this){
				double dist = planet.loc.distanceSq(loc);
				Vector2D g = loc.subtract(planet.loc).unitVector()
						.scalarMult(-RocketPanel.GRAVITATIONAL_CONSTANT * ((planet.getMass()) / dist));
				gravity = gravity.add(g);
			}
		}
		return gravity;
	}
	
	@Override
	public String toString(){
		return String.format("{id: %d, radius: %d, color: %s}", id, radius, color.toString());
	}
}
