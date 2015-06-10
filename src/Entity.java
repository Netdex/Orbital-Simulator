import java.awt.Graphics2D;
import java.util.ArrayList;


public abstract class Entity {
	public Vector2D loc;
	public Vector2D vel;
	
	public Entity(){
		
	}
	
	public abstract void draw(Graphics2D g);
	
	public abstract Vector2D gravityVector(ArrayList<Planet> planets);
	
	public Planet getSOI(ArrayList<Planet> planets){
		Planet closest = planets.get(0);
		double dist = planets.get(0).loc.distanceSq(loc);
		for(int i = 1; i < planets.size(); i++){
			Planet planet = planets.get(i);
			double newDist = planet.loc.distanceSq(loc) - planet.radius * planet.radius;
			if(newDist < dist){
				closest = planet;
				dist = newDist;
			}
		}
		return closest;
	}
	
	public Vector2D gravityVector(Vector2D loc, ArrayList<Planet> planets){
		Vector2D gravity = new Vector2D(0,0);
		for(Planet planet : planets){
			double dist = planet.loc.distanceSq(loc);
			Vector2D g = loc.subtract(planet.loc).unitVector()
					.scalarMult(-RocketPanel.GRAVITATIONAL_CONSTANT * (planet.getMass()/dist));
			gravity = gravity.add(g);
		}
		return gravity;
	}
}
