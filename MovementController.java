import lejos.hardware.motor.*;
import lejos.utility.Delay;

public class MovementController
{
	
	//
	//	Current things that could be improved:
	//		- Could have two arrays of motors, on called mLeft and mRight
	//		  so that we could know which side is which and the user would 
	//        just parse them in for our use and they specify it as currently only 1 motor
	//		  spins in the opposite way for the turning which for 2 wheels works fine
	//
	//		- Add a move to coord function which will either go directly to the point specified
	//		  It'll change the angle to face the new point and then go the distance to it 
	//
	// Constants - these would change depending on the real world values 
	
	
	final double wheelRadius = 2.7;  //cm
	final int wheelCircum = (int) (Math.PI * 2 *wheelRadius);
	
	final double distanceBetweenWheels = 11.0;	//cm  []-----+-----[] 
											//	      <-------------> this distance between the 2 motored wheels
	// Private Vars
	private int[] coords;
	private int currentAngle;
	private double wheelSpeed = 100.0;	//  Degrees/sec lower speed means things take more time but are more accurate
	private double actualSpeed;
	private BaseRegulatedMotor[] Motors;
	
	//Init
	public MovementController(BaseRegulatedMotor[] motors) {
		this.coords = new int[] {0, 0};
		this.currentAngle = 0;
		this.Motors = motors;
		
		for(BaseRegulatedMotor M : this.Motors) {
			M.setSpeed((int) this.wheelSpeed);
		}
		this.calculateSpeeds();
		
	}
	
	//Coords getter
	public int[] getCoords() {
		return this.coords;
	}
	
	//Angle getter
	public int getAngle() {
		return this.currentAngle;
	}
	
	public void setSpeed(double speed) {
		for(BaseRegulatedMotor M : this.Motors) {
			M.setSpeed((int) speed);
		}
		this.wheelSpeed = speed;
		this.calculateSpeeds();
	}
	
	//Updates vars for actualSpeed value 
	private void calculateSpeeds() {
		double percentSpeed = (this.wheelSpeed / 360.0);     // Because this was just 360 and not 360.0, divisions couldnt be a double, it always rounded like an integer            

		this.actualSpeed = wheelCircum * percentSpeed;	// cm/s
	}
	
	//calculates time for the wheels to spin for any angle of turn
	private double findDegreesForTurn(double angle) {

	    double percentTurn = (angle) / 360.0;             //Number 13:  Because this was just 360 and not 360.0, divisions couldnt be a double, it always rounded like an integer

	    double distance = (percentTurn * (Math.PI * distanceBetweenWheels));      

	    double timeOnPerWheel = (distance / this.actualSpeed);
	    
	    double degreesPerWheel = timeOnPerWheel * this.wheelSpeed;
	    
	    return degreesPerWheel;
	}
	
	//calculates tiem for the wheels to spin to travel any distance
	private double findDegreesForDistance(double distance) {
		
		double timeOnPerWheel = (distance / this.actualSpeed);
		
	    double degreesPerWheel = timeOnPerWheel * this.wheelSpeed;
		
		return degreesPerWheel;
	}
	
	//turns the vehicle be that angle
	public void turnAngle(double angle) {
		
		//check that angle isn't greater then 360 or -360
		if(angle >= 0)
			angle = angle % 360;
		
		else
			angle = angle % -360;
		
		BaseRegulatedMotor MainMotor = this.Motors[0];	//Sets main motor to sync with
		
		BaseRegulatedMotor SyncedMotors[] = new BaseRegulatedMotor[Motors.length - 1];	
    	
    	for(int i = 0; i < SyncedMotors.length; i++) {
    		SyncedMotors[i] = Motors[i+1];				//gets all other motors  
    	}
    	
    	double degreesForMovement = this.findDegreesForTurn(Math.abs(angle));
    	
    	// Sets all motors to be synced and run
    	MainMotor.synchronizeWith(SyncedMotors);
		MainMotor.startSynchronization();
		
		if(angle >= 0) {				// Clockwise
			MainMotor.rotate((int)(-degreesForMovement));
			
			for(BaseRegulatedMotor M : SyncedMotors) {
				M.rotate((int)(degreesForMovement));
			}
		}
		
		else {						// Counter-Clockwise
			MainMotor.forward();
			
			for(BaseRegulatedMotor M : SyncedMotors) {
				M.backward();
			}
		}
		
		MainMotor.endSynchronization();		// the motors start moving
		
		//uses the timeForDelay to make the motor move the correct amount
		//Delay.msDelay(delayForMovement);
		MainMotor.waitComplete();
		for(BaseRegulatedMotor M : SyncedMotors) {
			M.waitComplete();
		}

		//uses the same sync to stop the motors on time 
//		MainMotor.startSynchronization();
//		MainMotor.stop();
//		
//		for(BaseRegulatedMotor M : SyncedMotors) {
//			M.stop();
//		}
//		MainMotor.endSynchronization();
		
		this.updateCurrentAngle(angle);
		
	}
	
	//go's forward by that distance
	public void goForward(double distance) {
		
		BaseRegulatedMotor MainMotor = this.Motors[0];	//Sets main motor to sync with
		
		BaseRegulatedMotor SyncedMotors[] = new BaseRegulatedMotor[Motors.length - 1];	
    	
    	for(int i = 0; i < SyncedMotors.length; i++) {
    		SyncedMotors[i] = Motors[i+1];				//gets all other motors  
    	}
    	
    	double degreesForMovement = this.findDegreesForDistance(distance);
    	
    	// Sets all motors to be synced and run
    	MainMotor.synchronizeWith(SyncedMotors);
		MainMotor.startSynchronization();
		MainMotor.rotate((int)degreesForMovement);
		
		for(BaseRegulatedMotor M : SyncedMotors) {
			M.rotate((int)degreesForMovement);
		}
		MainMotor.endSynchronization();
		
		//uses the timeForDelay to make the motor move the correct amount
		//Delay.msDelay(delayForMovement);
		
		MainMotor.waitComplete();
		for(BaseRegulatedMotor M : SyncedMotors) {
			M.waitComplete();
		}
		
		//uses the same sync to stop the motors on time 
//		MainMotor.startSynchronization();
//		MainMotor.stop();
//		
//		for(BaseRegulatedMotor M : SyncedMotors) {
//			M.stop();
//		}
//		MainMotor.endSynchronization();
		
		this.updateCoords(distance, this.currentAngle);
		
	}
	
	//updates the local object coords var
	private void updateCoords(double distance, double angle) {
		// Find the coord from our current pos based on the angle we drove and how far
		//
		//			x2		x1 			= {0, 0}
		//		   / |		angle 		= 20'
		//		  /  |h		distance 	= 20
		//		 /   |		
		//		x1---+		height 		= sin(angle) * distance
		//		   w		width  		= cos(angle) * distance
		//					.: x2 		= {x1[0] + width, x2[1] + height}
		//		   
		// We need w and h to know to find where x2 would be
		// Then we need to set our new coords
		
		double height = Math.sin(angle) * distance;
		double width = Math.cos(angle) * distance;
		
		this.coords = new int[] {(int) (this.coords[0] + width), (int) (this.coords[1] + height)};
		
	}
	
	//updaets the local objects angle var
	private void updateCurrentAngle(double angle) {
		this.currentAngle += angle;
		
		if(this.currentAngle > 360.0) {
			this.currentAngle -= 360.0;
		}
		
		if(this.currentAngle < 0.0) {
			this.currentAngle += 360.0;
		}
		
	}
	
}