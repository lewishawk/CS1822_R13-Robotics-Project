import lejos.hardware.motor.*;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

class movementtester{
	
	public static void main(String args[]) {
		
		BaseRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.A);
    	BaseRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.B);
		
    	BaseRegulatedMotor allMotors[] = new BaseRegulatedMotor[] {mLeft, mRight};
    	
		MovementController Mv = new MovementController(allMotors);
		//Mv.setSpeed(50);
		for(int i = 0; i < 5; i++) {
			
			
			Mv.goForward(20.0);
			Mv.turnAngle(90.0);
			String toDisplayCoords = String.format("Coords: {%s, %s}", Mv.getCoords()[0], Mv.getCoords()[1]);
			String toDisplayAngle = String.format("Angle : %s", Mv.getAngle());
			
			LCD.clear();
			LCD.drawString(toDisplayCoords, 0, 0);
			LCD.drawString(toDisplayAngle, 0, 1);
			LCD.drawString(String.format("Battery: %s ", (Battery.getVoltage() / 12.0) * 100), 0, 2);
			Delay.msDelay(250);
			// With this code, the robot ends up back at the start position however is overturning slighlty
			// gonna have to change it to use .rotate and use degrees angle instead of time to make it a bit more accurate
			
		}
		
		Delay.msDelay(5000);
		
		mLeft.close();
		mRight.close();
		
		Button.ENTER.waitForPressAndRelease();
		
	}
}