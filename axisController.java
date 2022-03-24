import java.util.HashMap;
import java.util.Map;

import lejos.hardware.Button;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.SampleProvider;

public class axisController{

	final private 	int 							MOTOR_SPEED	= 10;
	final private	int 							MOTOR_MOVEMENT_SPEED = 1;
	final private 	int 							MOTOR_ACCEL	= 1000;
	
	private 		BaseRegulatedMotor				axisMotor;
	private 		EV3TouchSensor[]				axisSensors;
	private			EV3TouchSensor					axisRearSensor;
	private			EV3TouchSensor					axisFrontSensor;

	private 		int 							CalibratedDistance;
	private 		double 							axisDistanceMoved;
	private			Map<EV3TouchSensor, Integer>	axisSensorDistances;			
	
	public axisController(BaseRegulatedMotor Motor, EV3TouchSensor[] Sensors) {
		// init
		this.axisMotor		= Motor;
		this.axisMotor.setAcceleration(MOTOR_ACCEL);
		this.axisMotor.setSpeed(MOTOR_SPEED);
		
		this.axisSensors	= Sensors;
		this.axisSensorDistances = new HashMap<EV3TouchSensor, Integer>();
		shouldEnd();
		this.CalibrateMotor();
		
	}
	
	public BaseRegulatedMotor getMotor() {
		return this.axisMotor;
	}
	
	public void setSync(BaseRegulatedMotor[] Motors) {
		this.axisMotor.synchronizeWith(Motors);
	}
	
	public void startSync() {
		this.axisMotor.startSynchronization();
	}
	
	public void endSync() {
		this.axisMotor.endSynchronization();
	}
	
	private void CalibrateMotor() {
		
		//	For calibrating the motors distance from each side
		//	We use two touch sensors to detect when we're at the end of the axis
		//		
		//	So we go one way till 1 sensor goes off, then set that to be our 0
		//	Then go the other way until the other sensor goes off, then we use how many degrees we moved as out distance
		//	Then we know out distance and which sensor is at 0 and which is at max_length
		//
		
		//	Get Sensors Sample Providers
		SampleProvider TouchSen1 = this.axisSensors[0].getTouchMode();
		SampleProvider TouchSen2 = this.axisSensors[1].getTouchMode();
		float[] samples = new float[] {0f, 0f};
		
		int degreeCounter = 0;
		System.out.println("Starting Calibration");
		while(true) {
			//	Get Samples
			TouchSen1.fetchSample(samples, 0);
			TouchSen2.fetchSample(samples, 1);
			
			//	If Sensor 0 hits wall
			if(samples[0] > 0.8f) {
				
				//	Add sensor 0 to dictinary for distance and set to 0
				this.axisSensorDistances.put(this.axisSensors[0], 0);
				this.axisRearSensor = this.axisSensors[0];
				this.axisMotor.resetTachoCount();
				
				while(true) {
					shouldEnd();
					System.out.println(degreeCounter);
					//Using the other sensor, go until it hits something
					TouchSen2.fetchSample(samples, 1);
					if(samples[1] > 0.8f) {
						//	Once hit wall, add to dictionary and use counter to know its distance
						this.axisSensorDistances.put(this.axisSensors[1], this.axisMotor.getTachoCount());
						this.axisFrontSensor = this.axisSensors[1];
						System.out.println("Calibration Done");
						break;
					}
					this.axisMotor.rotate(MOTOR_MOVEMENT_SPEED, true);
					this.axisMotor.waitComplete();
					degreeCounter+=MOTOR_MOVEMENT_SPEED;

				}
				break;
			}
			
			// Same thing but for if the sensors are the other way round
			shouldEnd();
			if(samples[1] > 0.8f) {
				this.axisSensorDistances.put(this.axisSensors[1], 0);
				this.axisRearSensor = this.axisSensors[1];
				this.axisMotor.resetTachoCount();
				while(true) {
					shouldEnd();
					System.out.println(degreeCounter);
					TouchSen1.fetchSample(samples, 0);
					if(samples[0] > 0.8f) {
						this.axisSensorDistances.put(this.axisSensors[0], this.axisMotor.getTachoCount());
						this.axisFrontSensor = this.axisSensors[0];
						System.out.println("Calibration Done");
						break;
					}
					this.axisMotor.rotate(MOTOR_MOVEMENT_SPEED, true);
					this.axisMotor.waitComplete();
					degreeCounter+=MOTOR_MOVEMENT_SPEED;

				}
				break;
			}
			shouldEnd();
			this.axisMotor.rotate(-MOTOR_MOVEMENT_SPEED, true);
			this.axisMotor.waitComplete();
		}
		
		//	Set this so we know its distance
		this.CalibratedDistance = degreeCounter;
		System.out.println("Calibrated Distance to be " + this.CalibratedDistance);
		
		//	We now have the distance, a front and rear sensor therefore 
		//	we can now have functions to move until rear sensor goes off
		
		this.goToOrigin();
		
	}
	
	private void goToOrigin() {
		
		//	Sends the motor backwards whilst checking the sensor  
		//	it'll keep going backwards until the sensor is hit
		//	once hit the move amount is set to 0 and its at the origin
		shouldEnd();
		SampleProvider rearSample = this.axisRearSensor.getTouchMode();
		float[] sample = new float[] {0.0f};
		
		while(sample[0] < 0.8f) {
			shouldEnd();
			this.axisMotor.rotate(-MOTOR_MOVEMENT_SPEED, true);
			rearSample.fetchSample(sample, 0);
		}
		
		this.axisDistanceMoved = 0.0;
	}
	
	public void goDegrees(int Degrees) {
		
		//	Picks a sensor to use based on whether were going forwards or backwards
		//	Then moving a certain amount each time, we move the desired amount
		//	We use the sensor to make sure we can make that move
		//	if the sensor is hit, the loop will break ending the move
		shouldEnd();
		EV3TouchSensor toCheck = Degrees > 0 ? this.axisFrontSensor : this.axisRearSensor;
		SampleProvider Checker = toCheck.getTouchMode();
		
		float samples[] = new float[1];
		System.out.println("Moving " + Degrees + "*");
		
		for(int i = 0; i < Math.abs(Degrees); i+=MOTOR_MOVEMENT_SPEED) {
			
			Checker.fetchSample(samples, 0);
			System.out.println("Turning Motor : time " + i);
			shouldEnd();
			if(Degrees > 0) {
				if(samples[0] > 0.8f) {
					break;
				}
				
				this.axisMotor.rotate(MOTOR_MOVEMENT_SPEED, false);
				this.axisDistanceMoved+= MOTOR_MOVEMENT_SPEED;
			} 
			else {
				
				if(samples[0] > 0.8f) {
					break;
				}
				
				this.axisMotor.rotate(-MOTOR_MOVEMENT_SPEED, false);
				this.axisDistanceMoved-= MOTOR_MOVEMENT_SPEED;
			}
			shouldEnd();
			this.axisMotor.waitComplete();
		}
		
	}
	
	public double getCurrentLocation() {
		return this.axisDistanceMoved;
	}
	
	public int getAxisLength() {
		return this.CalibratedDistance;
	}
	
	private void shouldEnd() {
		if(Button.ENTER.isDown()) {
			System.exit(0);
		}
	}
}