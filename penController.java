import lejos.hardware.motor.BaseRegulatedMotor;

enum penState{
	
	//	Using an enum for a statemachine for our pen 
	//	with the moveDegrees how far it needs to move 
	//	to put the pen into that position
	
	UP(100), DOWN(-100);
	
	public int moveDegrees;
	
	private penState(int degrees) {
		this.moveDegrees = degrees;
	}
}




public class penController {

	private penState currentState;
	
	private BaseRegulatedMotor motor;
	
	public penController(BaseRegulatedMotor Motor) {
		this.motor = Motor;
		
		this.currentState = penState.UP;
	}
	
	
	public boolean isPenUp() {
		return currentState == penState.UP;
	}
	
	public boolean putPenDown() {
		
		//	Moves the pen to the down position
		
		if(this.isPenUp()) {
			this.currentState = penState.DOWN;
			this.motor.rotate(this.currentState.moveDegrees, false);
			this.motor.waitComplete();
			return true;
		}
		
		return false;
		
	}
	
	public boolean putPenUp() {
		
		//	moves the pen to the up position
		
		if(!this.isPenUp()) {
			this.currentState = penState.UP;
			this.motor.rotate(this.currentState.moveDegrees, false);
			this.motor.waitComplete();
			return true;
		}
		return false;
		
	}
}