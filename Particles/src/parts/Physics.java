package parts;

public class Physics {
	
	static float totalChange = 0; // sums the total error in particle on particle collisions
	
	static Collision tempC = new Collision();
	
	// check if the particle collides with the bounding box
	public static void checkBoxCollision(float xpos, float ypos, float xvol, float yvol,  float xacc, float yacc,float radius, float xmin, float ymin,
			float xmax, float ymax, float t, Collision C){
		
		// right
		checkVerticalLine(xpos, xvol, yvol, xacc, yacc, radius, xmax, t);
		if(tempC.t < C.t){
			C.copy(tempC);
		}
		
		// left
		checkVerticalLine(xpos, xvol, yvol, xacc, yacc, radius, xmin, t);
		if(tempC.t < C.t){
			C.copy(tempC);
		}
		
		// up
		checkHorizontalLine(ypos, xvol, yvol, xacc, yacc, radius, ymin, t);
		if(tempC.t < C.t){
			C.copy(tempC);
		}

		// down
		checkHorizontalLine(ypos, xvol, yvol, xacc, yacc, radius, ymax, t);
		if(tempC.t < C.t){
			C.copy(tempC);
		}

	}
	
	// check if a particle hits a vertical line
	public static void checkVerticalLine(float xpos, float xvol, float yvol, float xacc, float yacc, float radius, float linex,
			float tmax){

		tempC.reset(); // reset collision holder

		if(xpos == 0 && xacc == 0){ // no collision if nothing moves
			return;
		}

		float distance; // distance from border (negative is a left move, positive is a right move)
		if(linex < xpos){
			distance = xpos - (linex + radius);
		}
		else{
			distance = xpos - (linex - radius);
		}

		if(xacc == 0){
			distance = -1*distance;
			float timetocollision = distance/xvol;
			if(timetocollision>0 && timetocollision<=tmax){
				tempC.t = timetocollision;
				tempC.nspeedx = -1*(xvol); // reflect x
				tempC.nspeedy = yvol; // leave y
			}
		}
		else{
			// fix here for gravity
			float t1 = (-1*xvol + (float)Math.sqrt((xvol*xvol) - (2*xacc*distance))) / xacc; // time until collision
			float t2 = (-1*xvol - (float)Math.sqrt((xvol*xvol) - (2*xacc*distance))) / xacc;
			if(linex<xpos){
				System.out.format("xpos: %f xvol: %f xacc: %f%n", xpos, xvol, xacc);
				System.out.format("t1: %f t2 %f tmax: %f%n", t1, t2, tmax);
			}
			if(t1 > 0 && t1 <= t2 && t1 <= tmax){ // if collision occurs during time interval
				tempC.t = t1;
				tempC.nspeedx = -(xvol + xacc*t1); // reflect x
				tempC.nspeedy = yvol + yacc*t1; // leave y
			}
			else if(t2>0 && t2<=tmax){
				tempC.t = t2;
				tempC.nspeedx = -(xvol + xacc*t2); // reflect x
				tempC.nspeedy = yvol + yacc*t2; // leave y
			}
		}
	}

	// check if a particle hits a horizontal line
	public static void checkHorizontalLine(float ypos, float xvol, float yvol, float xacc, float yacc, float radius, float liney,
			float tmax){
		
		tempC.reset(); // reset collision holder
		
		if(yvol == 0 && yacc == 0){ // no collision if nothing moves
			return;
		}
		
		float distance; // distance from border (negative is an up move, positive is a down move)
		if(liney < ypos){
			distance = ypos - (liney + radius);
		}
		else{
			distance = ypos - (liney - radius);
		}
		
		if(yacc == 0){
			distance = -1*distance;
			float timetocollision = distance/yvol;
			if(timetocollision>0 && timetocollision<=tmax){
				tempC.t = timetocollision;
				tempC.nspeedx = xvol; // leave x
				tempC.nspeedy = -1*(yvol); // reflect y
			}
		}
		else{
			// fix here for gravity
			float t1 = (float)((-1*yvol + Math.sqrt((yvol*yvol) - (2*yacc*distance))) / yacc); // time until collision
			float t2 = (float)((-1*yvol - Math.sqrt((yvol*yvol) - (2*yacc*distance))) / yacc);
			if(t1 > 0 && t1 <= t2 && t1 <= tmax){ // if collision occurs during time interval
				tempC.t = t1;
				tempC.nspeedx = xvol + xacc*t1; // leave x
				tempC.nspeedy = -1*(yvol + yacc*t1); // reflect y
			}
			else if(t2>0 && t2<=tmax){
				tempC.t = t2;
				tempC.nspeedx = xvol + xacc*t2; // leave x
				tempC.nspeedy = -1*(yvol + yacc*t2); // reflect y
			}
		}
	}
	
	// check if a point hits another point
	public static void pointIntersectsPoint(Particle p1, Particle p2, Collision p1Collision, Collision p2Collision, float timeLimit){
		// difference in x and y positions
		float Cx = p2.xpos - p1.xpos;
		float Cy = p2.ypos - p1.ypos;
		// difference in x and y velocities
		float Vx = p2.xvol - p1.xvol;
		float Vy = p2.yvol - p1.yvol;
		// distance between particles' centers
		float r = p1.radius + p2.radius;
		
		if(Vx == 0 && Vy == 0){
			return; // will never collide
		}
		// create a quadritc function (some reductions already done
		float part1 = -1*(Cx*Vx + Cy*Vy); // b (about)
		float part2 = r*r*(Vx*Vx + Vy*Vy) + 2*Cx*Cy*Vx*Vy - Vx*Vx*Cy*Cy - Vy*Vy*Cx*Cx; // b2 - 4ac (about)
		float part3 = Vx*Vx + Vy*Vy; // a (about)
		
		// solve for both solutions (t is time of collision)
		float t1 = (part1 + (float)Math.sqrt(part2))/part3;
		float t2 = (part1 - (float)Math.sqrt(part2))/part3;
		
		// take the earliest positive collision if it is within the time step
		if(t1 > 0 && t1 <= t2 && t1 <= timeLimit){
			// store time in the collisions and calculate response
			p1Collision.t = t1;
			p2Collision.t = t1;
			pointIntersectPointResponse(p1, p2, p1Collision, p2Collision, t1);
		}
		else if(t2>0 && t2 <= timeLimit){
			// store time in the collisions and calculate response
			p1Collision.t = t2;
			p2Collision.t = t2;
			pointIntersectPointResponse(p1, p2, p1Collision, p2Collision, t2);
		}

	}
	
	// calculate the new velocities of the particles after collision
	public static void pointIntersectPointResponse(Particle p1, Particle p2, Collision p1Collision, Collision p2Collision, float time){
		// create normal vector (vector in direction of the collision (p2.x - p1.x, p2.y - p1.y)
		float[] n = {(p2.xpos + (p2.xvol * time)) - (p1.xpos + (p1.xvol * time)), (p2.ypos + (p2.yvol * time)) - (p1.ypos + (p1.yvol * time))};
		// get the magnitude of the vector
		float mag = (float)(Math.sqrt(n[0]*n[0] + n[1]*n[1]));
		// create the unit normal vector (normal/mag)
		n[0] = n[0]/mag;
		n[1] = n[1]/mag;
		
		// create the unit tangent vector from the normal tangent vector, this is the vector tangent
		// to the collision which does change during the collision
		float[] t = {-1*n[1], n[0]};
		
		// reduce the velocities along the unit normal and unit tangent vectors
		float v1 = p1.xvol*n[0] + p1.yvol*n[1]; // p1 velocity along collision vector
		float v2 = p2.xvol*n[0] + p2.yvol*n[1]; // p2 velocity along collision vector
		float t1 = p1.xvol*t[0] + p1.yvol*t[1]; // p1 velocity tangent to collision vector
		float t2 = p2.xvol*t[0] +  p2.yvol*t[1]; // p2 velocity tangent to collision vector
		float m1 = p1.radius*p1.radius*p1.radius; // mass of p1
		float m2 = p2.radius*p2.radius*p2.radius; // mass of p2
		float v3, v4;
		
		// exchange velocities if masses are equal
		if(m1 == m2){
			v3 = v2;
			v4 = v1;
		}
		else{
			// calculate new velocities
			v4 = ((2*m1*v1) + (m2 - m1)*v2)/(m1+m2);
			v3 = ((2*m2*v2) + (m1 - m2)*v1)/(m1+m2);
		}
		//float[] t1f = {t1*t[0], t1*t[1]};
		//float[] t2f = {t2*t[0], t2*t[1]};
		// recombine normal velocities with tangent velocities by multiplying with unit vector again
		float[] p1Vols = {v3*n[0]+t1*t[0], v3*n[1]+t1*t[1]};
		float[] p2Vols = {v4*n[0]+t2*t[0], v4*n[1]+t2*t[1]};
		
		// test accuracy of result
		float p1V = p1.xvol*p1.xvol + p1.yvol*p1.yvol;
		float p2V = p2.xvol*p2.xvol + p2.yvol*p2.yvol;
		float p1Vf = p1Vols[0]*p1Vols[0] + p1Vols[1]*p1Vols[1];
		float p2Vf = p2Vols[0]*p2Vols[0] + p2Vols[1]*p2Vols[1];
		float momentumBefore = p1V*m1 + p2V*m2;
		float momentumAfter = p1Vf*m1 + p2Vf*m2;
		if(momentumBefore == momentumAfter){
			System.out.println("GOOD");
		}
		else{
			totalChange += momentumAfter - momentumBefore;
			System.out.format("Total change in energy: %f%n", totalChange);
		}
		
		// store new velocities in collision
		p1Collision.nspeedx = p1Vols[0];
		p1Collision.nspeedy = p1Vols[1];
		
		p2Collision.nspeedx = p2Vols[0];
		p2Collision.nspeedy = p2Vols[1];
	}
	

}
