package parts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JPanel;

// Controls the box in which particles bounce around
public class BoxPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int UPDATE_RATE = 30; // Frames per second 
	private static final float EPSILON_TIME = 1e-3f; // 0.01
	
	private static final int ARROW_THICKNESS = 5; // thickness of the arrow line when placing particles
	private static final int ARROW_SIZE = 15; // size of the arrows head
	
	private static final Color[] colors = {Color.BLACK, Color.YELLOW, Color.ORANGE, Color.RED, Color.MAGENTA, Color.BLUE,
			Color.GREEN,}; // possible colors of generated particles
	
	private BufferedImage bg; // background of the box
	//Graphics2D graphics; // graphics for drawing on the background
	public int width; // width of box
	public int height; // height of box
	private LinkedList<Particle> particles; // list of all particles in the box
	private boolean go; // True means time is flowing, false means time has stopped
	
	private int state; // 0 = normal mouse 1 = place particles 2 = placing particle (mouse is held down)
	private int startx, starty, endx, endy; // coordinates of beginning and ending of arrow

	public BoxPanel(int width, int height) {
		this.width = width;
		this.height = height;
		
		// set up box with solid white background
		bg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = bg.createGraphics();
		graphics.setPaint(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		setPreferredSize(new Dimension(width, height));
		repaint();
		
		// listens for clicks and drags in the box
		addMouseListener(new BoxClickListener()); 
		addMouseMotionListener(new BoxMotionAdapter());
		
		// bring it to life
		particles = new LinkedList<Particle>();
		go = false;
		state = 0;
		startx = -1;
		starty = -1;
		
		// test particles
		//newParticle(20, 20, 10, 10, 315, Color.BLACK);
		//newParticle(20, 380, 10, 10, 45, Color.BLACK);
		//straight
		//newParticle(10, 100, 5, 10, 0, Color.BLACK);
		//newParticle(100, 100, 5, 0, 0, Color.BLACK);
	}
	
	// change the state of the box
	public void setState(int s){
		this.state = s;
	}
	
	// draw each particle
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		// draw BG
		g2.drawImage(bg, 0, 0, width, height, this);
		// draw each particle
		for(Particle p: particles){
			p.draw(g2);
		}
		// draw an arrow if mouse is being drag in palce particle mode
		if(state == 2){
			drawArrow(g2);
		}
	}
	
	public void update(){
		float timeleft = 1.00f; // 100%
		do{
			// Check if each particle hits the box boundaries (must be done first as it resets collision)
			float firstCollisionTime = timeleft;
			
			// Check if any particle hits another
			int i, j;
			for(i=0; i<particles.size(); i++){
				for(j=i; j<particles.size(); j++){
					if(i!=j){
						particles.get(i).intersects(particles.get(j), timeleft);
						if(particles.get(i).firstCollision.t < firstCollisionTime){
							firstCollisionTime = particles.get(i).firstCollision.t;
						}
					}
				}
			}
			// check for collisions with the bounding box
			for(Particle p: particles){
				p.checkCollisions(this, timeleft);
				if(p.firstCollision.t < firstCollisionTime){
					firstCollisionTime = p.firstCollision.t;
				}
			}
			// move each particle the up to the time of the first collision
			for(Particle p: particles){
				p.move(firstCollisionTime);
				p.reset();
			}
			// update remaining portion of time step to check
			timeleft -= firstCollisionTime;
			
		}while(timeleft > EPSILON_TIME); // until entire time step checked
		
	}
	
	// start time
	public void play(){
		if(!go){ // make sure not already playing
			go = true;
			// start simulation thread
			Thread time = new Thread(){
				synchronized public void run(){
					while(true){
						
						long startTime, timeTaken, timeLeft;
						startTime = System.currentTimeMillis(); // get start time of tick
						
						// update positions
						update(); 
						repaint();
						
						timeTaken = System.currentTimeMillis() - startTime; // get time taken to update
						// time left after updating
						timeLeft = 1000L / UPDATE_RATE - timeTaken;
						
						try{ // wait for amount of time left in the tick
							sleep(timeLeft);
						}
						catch(Exception e){
							e.printStackTrace();
						}
						if(!go){ // go turned to false, stop
							break;
						}
						
					}
				}
			};
			// start simulation thread
			time.start();
		}
	}
	
	// pauses time
	public void pause(){
		go=false; // switching this boolean lets the current time thread terminate
	}
	
	// creates a new particle in the box
	synchronized public Particle newParticle(float xpos, float ypos, float radius, float velocity, float angle, Color color){
		if(xpos <= 0 + radius || xpos >= width - radius){
			System.out.println("Invalid xpos");
			return null;
		}
		if(ypos <= 0 + radius || ypos >= height - radius){
			System.out.println("Invalid ypos");
			return null;
		}
		Particle p = new Particle(xpos, ypos, radius, velocity, angle, color);
		particles.add(p);
		return p;
	}
	
	// removes a particle from the box
	synchronized public void removeParticle(Particle p){
		particles.remove(p);
	}
	
	// generate a new random particle
	public void newRandomParticle(){
		float randr = (float)(Math.random()*30 + 10);
		float xrange = width - (2*randr) - 0.005f;
		float yrange = height - (2*randr) - 0.005f;
    	float randx = (float)(Math.random()*xrange + randr);
    	float randy = (float)(Math.random()*yrange + randr);
    	float randv = (float)(Math.random()*20);
    	float randa = (float)(Math.random()*360);
    	Color c = colors[(int)(Math.random()*7)];
        newParticle(randx, randy, randr, randv, randa, c);
        repaint();
	}
	
	// deletes all selected particles
	public void deleteSelected(){
		Particle p;
		for(int i=0; i<particles.size(); i++){
			p = particles.get(i);
			if(p.isSelected){
				removeParticle(p);
				i--;
			}
		}
		repaint();
	}
	
	// deletes all particles
	public void clearAll(){
		this.particles = new LinkedList<Particle>();
		repaint();
	}
	
	// checks if a particle is under the given coordinates
	public boolean isParticleUnderClick(int x, int y){
		double xdist, ydist, distance;
		for(Particle p: particles){ // check each particle for if the click was within its radius
			xdist = x - p.xpos;
			ydist = y - p.ypos;
			distance = (xdist*xdist) + (ydist*ydist);
			if(distance <= p.radius*p.radius){
				return true;
			}
		}
		return false;
	}
	
	// draws an arrow between a mouse click and mouse position
	private void drawArrow(Graphics2D g2){
		// draw line from mouse press to mouse location
		g2.setPaint(Color.RED);
		g2.setStroke(new BasicStroke(ARROW_THICKNESS));
		g2.drawLine(startx, starty, endx, endy);
		
		// calculate and draw arrow heads
		float diffx = startx - endx;
		float diffy = starty - endy;
		float angle;
		if(diffx == 0){ // account for divide by zero issues
			if(diffy > 0){ // straight up
				angle = (float)(Math.PI/2);
			}
			else{ // straight down
				angle = (float)(3*Math.PI/2);
			}
		}
		else{
			// angle = arctan(Y/X)
			angle = (float)(Math.atan(-1*diffy/diffx)); //(-y for inverted y axis)
			if(diffx > 0){ // because of how arctan works
				angle = (float)(Math.PI + angle);
			}
		}
		
		// spot that is ARROW_SIZE away from the tip of the line
		float newx = (float) (endx - (ARROW_SIZE*Math.cos(angle)));
		float newy = (float) (endy + (ARROW_SIZE*Math.sin(angle)));
		// distances out from this spot
		diffx = (float) (ARROW_SIZE*Math.sin(angle));
		diffy = (float) (ARROW_SIZE*Math.cos(angle));
		// combine the distances and spots and draw lines to the tip of the line
		g2.drawLine((int)(newx - diffx), (int)(newy - diffy), endx, endy);
		g2.drawLine((int)(newx + diffx), (int)(newy + diffy), endx, endy);
	}
	
	private class BoxClickListener extends MouseAdapter{
		// Starts the visuals for placing a particle when mouse is in particle placing mode
		@Override
		public void  mousePressed(MouseEvent e){
			if(state == 1){
				startx = e.getX();
				starty = e.getY();
				state = 2;
			}
		}
		
		// detect mouse clicks in the box
		@Override
		public void mouseReleased(MouseEvent e){
			// restores state if placing a particle
			if(state == 2){
				state = 1;
			}
			
			// check each particle for if the click was within its radius
			double xdist, ydist, distance;
			for(Particle p: particles){ 
				xdist = e.getX() - p.xpos;
				ydist = e.getY() - p.ypos;
				distance = (xdist*xdist) + (ydist*ydist);
				if(distance <= p.radius*p.radius){
					// toggle the selected state of the particle
					if(p.isSelected){
						p.deselect();
					}
					else{
						p.select();
					}
				}
			}
			// update to show color change
			repaint();
		}
	}
	
	// updates location of mouse if placing a new particle
	private class BoxMotionAdapter extends MouseMotionAdapter{
		@Override
		public void mouseDragged(MouseEvent e){
			if(state == 2){
				endx = e.getX();
				endy = e.getY();
				repaint();
			}
		}
	}


}
