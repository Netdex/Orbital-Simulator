import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;


public class Particle extends Entity {
	
	private Color color;
	public int maxLifespan;
	public int lifespan;
	public int size;
	
	public Particle(Vector2D loc, Vector2D vel, Color color, int lifespan, int size){
		this.loc = loc;
		this.vel = vel;
		this.color = color;
		this.lifespan = lifespan;
		this.size = size;
		this.maxLifespan = lifespan;
	}
	
	public boolean tick(){
		this.loc = this.loc.add(this.vel);
		for(Planet planet : RocketPanel.sim.planets){
			if(planet.loc.distance(loc) < planet.radius){
				double diffX = planet.loc.x - loc.x;
				double diffY = planet.loc.y - loc.y;
	
				Vector2D relative = new Vector2D(diffX, diffY);
				relative.setR(vel.getR() / RocketPanel.RESTITUTION);
				vel.x = -relative.x;
				vel.y = -relative.y;
				break;
			}
		}
		lifespan--;
		if(lifespan > 0)
			return true;
		return false;
	}
	
	public Vector2D gravityVector(ArrayList<Planet> planets){
		return null;
	}
	
	public Color getColor(){
		return color;
	}
	
	@Override
	public void draw(Graphics2D g){
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 
				Math.min((int)((double)lifespan / maxLifespan * 255), 255)));
		g.fillRect((int)loc.x, (int)loc.y, size, size);
	}
}
