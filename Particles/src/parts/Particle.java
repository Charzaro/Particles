package parts;

import java.awt.Color;
import java.awt.Graphics2D;

public class Particle {
	//this is a test
	
	// size
	float radius;
	// position
	float xpos;
	float ypos;
	// speed in x and y directions
	float xvol;
	float yvol;
	// color
	Color color;
	
	// for selecting and deselecting
	Color currentColor;
	boolean isSelected;
	
	public Collision firstCollision = new Collision(); // earliest collision
	private Collision tempCollision = new Collision(); // for testing a new collision, before we know if it is earlier
	

	
	public Particle(float xpos, float ypos, float radius, float velocity, float angleInDegrees, Color color) {
		this.radius = radius;
		this.xpos = xpos;
		this.ypos = ypos;
		
		this.color = color;
		
		currentColor = color;
		isSelected = false; // start deselected
		
		firstCollision.reset(); 
		// obtain velocities in x and y directions from angle and velocity
		xvol = (float)(velocity * Math.cos(Math.toRadians(angleInDegrees)));
		yvol = (float)(-1*velocity * Math.sin(Math.toRadians(angleInDegrees))); //-1 because positive y goes down

	}
	
	// reset both stored collisions
	public void reset(){
		firstCollision.reset();
		tempCollision.reset();
	}
	
	// checks if the particle will hit the box boundaries in the time remaining
	public void checkCollisions(BoxPanel box, float timelimit){
		Physics.checkBoxCollision(xpos, ypos, xvol, yvol, radius, 0, 0, (float)box.width,
				(float)box.height, timelimit, tempCollision);
		// only keep collision if it is the earliest collision
		if(tempCollision.t < firstCollision.t){
			firstCollision.copy(tempCollision);
		}
	}
	
	// check if the particle collides with a second particle in the remaining time
	public void intersects(Particle p, float timelimit){
		Collision anotherTemp = new Collision();
		Physics.pointIntersectsPoint(this, p, tempCollision, anotherTemp, timelimit);
		// only keep if it is the particle's earliest collision
		if(tempCollision.t < firstCollision.t){
			firstCollision.copy(tempCollision);
		}
		if(anotherTemp.t < p.firstCollision.t){
			p.firstCollision.copy(anotherTemp);
		}
	}

	public void move(float time){// could add a arg of the container and change 0 and 400 to box's bounds
		
		if(firstCollision.t <= time){ // if the particle collides in given time
			//System.out.println("BOOM"); //debug purposes
			xpos = firstCollision.getNewX(xpos, xvol);
			ypos = firstCollision.getNewY(ypos, yvol);
			xvol = firstCollision.nspeedx;
			yvol = firstCollision.nspeedy;
			//System.out.format("newx: %f newy: %f%n", xpos, ypos); // debug line
		}
		else{ // move without collision
			xpos += xvol * time;
			ypos += yvol * time;
		}
		
	}
	
	// select the particle
	public void select(){
		currentColor = Color.CYAN;
		isSelected = true;
	}
	
	// deselect particle
	public void deselect(){
		currentColor = color;
		isSelected = false;
	}
	
	// draw the particle
	public void draw(Graphics2D g2){
		g2.setPaint(currentColor);
		g2.fillOval((int)(xpos-radius), (int)(ypos-radius), (int)(2*radius), (int)(2*radius));
	}

}
