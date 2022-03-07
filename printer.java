//	
//	Printer With lejos
//	
//	3 Motors
//		- 2 Motors For X and Y : MotorPort A and MotorPort B
//		- 1 Motor For Pen Up and Pen Down : MotorPort C
//	
//	4 Touch Sensors
//		- 2 on Each Movement Motor : S1, S2 for X and S3, S4 for Y
//	


//	Imports

import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;


public class printer{
	

	//	Constants
	
	//	Motors for Movement 
	final private BaseRegulatedMotor MOVEMENT_MOTORS[] = new BaseRegulatedMotor[] {new EV3LargeRegulatedMotor(MotorPort.A), new EV3LargeRegulatedMotor(MotorPort.B) };
	
	//	Motor for the Pen
	final private BaseRegulatedMotor PEN_MOTOR = new EV3LargeRegulatedMotor(MotorPort.C);

	//	Touch Sensors for calibration on the X and Y Motors
	final private EV3TouchSensor MOVEMENT_SENSORS[] = new EV3TouchSensor[] {new EV3TouchSensor(SensorPort.S1), new EV3TouchSensor(SensorPort.S2),
																			new EV3TouchSensor(SensorPort.S3), new EV3TouchSensor(SensorPort.S4)};
	
	//	For Each Axis setting the Motors and Sensors for the axis Controllers
	final private axisController sideAxis = new axisController(MOVEMENT_MOTORS[0], new EV3TouchSensor[] {MOVEMENT_SENSORS[0], MOVEMENT_SENSORS[1]} );
	
	final private axisController topAxis = new axisController(MOVEMENT_MOTORS[1], new EV3TouchSensor[] {MOVEMENT_SENSORS[2], MOVEMENT_SENSORS[3]} );
	
	//	Then Using a Overall Controller for the axis's, this will make it be able to move paths
	//	So another class need sto be made which controls both axisControllers which we can interface with here
	
	
	public static void main(String args[]) {
		
	}
	
	
	
}