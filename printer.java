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

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;


public class printer{
	

	//	Constants
	
	//	Motors for Movement 
	final private static BaseRegulatedMotor MOVEMENT_MOTORS[] = new BaseRegulatedMotor[] {new EV3LargeRegulatedMotor(MotorPort.A), new EV3LargeRegulatedMotor(MotorPort.B) };
	
	//	Motor for the Pen
	final private static BaseRegulatedMotor PEN_MOTOR = new EV3LargeRegulatedMotor(MotorPort.C);

	//	Touch Sensors for calibration on the X and Y Motors
	final private static EV3TouchSensor MOVEMENT_SENSORS[] = new EV3TouchSensor[] {new EV3TouchSensor(SensorPort.S1), new EV3TouchSensor(SensorPort.S2),
																			new EV3TouchSensor(SensorPort.S3), new EV3TouchSensor(SensorPort.S4)};
	
	//	For Each Axis setting the Motors and Sensors for the axis Controllers
	final private static axisController sideAxis = new axisController(MOVEMENT_MOTORS[0], new EV3TouchSensor[] {MOVEMENT_SENSORS[0], MOVEMENT_SENSORS[1]} );
	
	final private static axisController topAxis = new axisController(MOVEMENT_MOTORS[1], new EV3TouchSensor[] {MOVEMENT_SENSORS[2], MOVEMENT_SENSORS[3]} );
	
	final private static penController pen = new penController(PEN_MOTOR);
	
	//	Then Using a Overall Controller for the axis's, this will make it be able to move paths
	//	So another class need sto be made which controls both axisControllers which we can interface with here
	
	
	public static void main(String args[]) {
		
		//	create the axisInterface
		//	gets the scale values from that for generating a circle later
		System.out.println("Printer : v12");
		System.out.println("-------------");
		System.out.println("-- Authors --");
		System.out.println("Joshua Barrass");
		System.out.println("Lewis Hawkesford");
		System.out.println("Iffet Aygun");
		
		Button.ENTER.waitForPressAndRelease();
		axisInterface printerController = new axisInterface(sideAxis, topAxis, pen);
		int choice = 1;
		
		while(!Button.ENTER.isUp())
		{
			LCD.clear();
			System.out.println(String.format("Choice: %s",  choice));
			System.out.println(choice == 1 ? "> Circle" : "  Circle");
			System.out.println(choice == 2 ? "> Square" : "  Sqaure");
			System.out.println(choice == 3 ? "> Boundries" : "  Boundries");
			
			while(!Button.ENTER.isUp()) {
				
				if(Button.DOWN.isDown()) {
					choice ++;
					if(choice > 3 )
						choice = 3;
					break;
				}
				
				if(Button.UP.isDown()) {
					choice--;
					if(choice < 1) {
						choice = 1;
					}
					break;
				}
				
			}
			
		}
		
		LCD.clear();
		
		switch(choice) {
			case 1:
				System.out.println("Drawing Circle");
				drawCircle(printerController);
				break;
			case 2:
				System.out.println("Drawing Square");
				drawSquare(printerController);
				break;
			case 3:
				System.out.println("Drawing Boundaries");
				drawBoundaries(printerController);
				break;
			default:
				
				break;
		
		}
		
		

		Button.ENTER.waitForPressAndRelease();

		
		
	}
	
	private static void drawBoundaries(axisInterface printerController) {
		
		Path newPath = new Path();
		double xMax = printerController.getXLength();
		double yMax = printerController.getYLength();
		newPath.add(new Waypoint(0,0));
		newPath.add(new Waypoint(xMax,0));
		newPath.add(new Waypoint(xMax, yMax));
		newPath.add(new Waypoint(0, yMax));
		newPath.add(new Waypoint(0,0));
		
		printerController.followPath(newPath, false);
	}
	
	private static void drawSquare(axisInterface printerController) {
		
		Path newPath = new Path();
		double xQuarter = printerController.getXLength()/4;
		double yQuarter = printerController.getYLength()/4;
		
		double xQuarterTo= printerController.getXLength()*(3/4);
		double yQuarterTo = printerController.getYLength()*(3/4);
		
		newPath.add(new Waypoint(xQuarter,yQuarter));
		newPath.add(new Waypoint(xQuarter, yQuarterTo));
		newPath.add(new Waypoint(xQuarterTo, yQuarterTo));
		newPath.add(new Waypoint(xQuarterTo, yQuarter));
		newPath.add(new Waypoint(xQuarter, yQuarter));
//+-	 -+		
//|	x---x |		(.25, .25)
//				(.25, .75)
//	x---x		(.75, .75)
//				(.75, .25)
//				(.25, .25)
		
		printerController.followPath(newPath, false);
	}
	
	private static void drawCircle(axisInterface printerController) {
		
		double xScalar = printerController.getXScalar();
		double yScalar = printerController.getYScalar();
		Path newPath = new Path();
		
		//	Everything we need to generate a circle and add it to a path for the robot
		
		double pointAmount = 36;
		double AngleChange = 360 / pointAmount ;
        double CircleRadius = printerController.getXLength() / 3.0;
        
        double CurrentAngle = 0.0;

        for(int i = 0; i < pointAmount; i++){
            
            double LocalX = (CircleRadius * Math.cos(Math.toRadians(CurrentAngle)));
            double LocalY = (CircleRadius * Math.sin(Math.toRadians(CurrentAngle)));
            
            newPath.add(new Waypoint((LocalX + CircleRadius) * xScalar, (LocalY + CircleRadius) * yScalar));

            CurrentAngle += AngleChange;
		}
        
        //	Makes it follow a path
        printerController.followPath(newPath, false);
	}
	
}