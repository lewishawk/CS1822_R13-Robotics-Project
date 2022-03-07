import lejos.hardware.motor.BaseRegulatedMotor;

enum penState{
		UP(-100), DOWN(100);
		
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
		
		if(this.isPenUp()) {
			this.currentState = penState.DOWN;
			this.motor.rotate(this.currentState.moveDegrees, false);
			this.motor.waitComplete();
			return true;
		}
		
		return false;
		
	}
	
	public boolean putPenUp() {
		
		if(!this.isPenUp()) {
			this.currentState = penState.UP;
			this.motor.rotate(this.currentState.moveDegrees, false);
			this.motor.waitComplete();
		}
		return false;
		
	}
}