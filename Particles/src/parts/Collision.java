package parts;

public class Collision {

	float t; // time of the collision
	float nspeedx; // new speed in x direction
	float nspeedy; // new speed in y direction
	
	float T_EPSILON = 0.005f; // small factor to ensure boundaries are not crossed

	public Collision(){
		reset();
	}

	// start with largest value for t so any collision is earlier
	public void reset(){
		t = Float.MAX_VALUE;
	}

	// copy over another collision to this collision
	public void copy(Collision c2){
		this.t = c2.t;
		this.nspeedx = c2.nspeedx;
		this.nspeedy = c2.nspeedy;
	}
	
	// get the new x position 
	public float getNewX(float xpos, float xvol, float xacc){
		if(t > T_EPSILON){
			float tround = (t - T_EPSILON);
			return (float) (xpos + xvol*tround + (0.5*xacc*tround*tround));
		}
		else{
			return xpos;
		}
	}
	
	// get the new y position
	public float getNewY(float ypos, float yvol, float yacc){
		if(t > T_EPSILON){
			float tround = (t - T_EPSILON);
			return (float) (ypos + yvol*tround + (0.5*yacc*tround*tround));
		}
		else{
			return ypos;
		}
	}

}
