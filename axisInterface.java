import java.util.ArrayList;

import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;

public class axisInterface{
	
	private axisController xAxis;
	private axisController zAxis;
	private penController  Pen;
	private ArrayList<String> functionCalls = new ArrayList<String>();
	
	public axisInterface(axisController xAxis, axisController zAxis, penController Pen) {
		
		//	takes in all needed controllers for the robot to move and function
		
		this.xAxis = xAxis;
		this.zAxis = zAxis;
		this.Pen = Pen;
		for(int i = 0; i < 6; i++) {
			this.functionCalls.add("None");
		}
		
		//	this is called after the axis controllers are calibrated
		
		System.out.println("Boundrys");
		System.out.println(this.xAxis.getAxisLength());
		System.out.println(this.zAxis.getAxisLength());
		
	}
	
	//	Due to issues with how the motors affect each other while moving, they have to move seperately
	//	Therefore they cannot by synced or be moving at the same time
	
	
	public void moveToPoint(Waypoint point, boolean Interpolate) {
		
		//	takes in a waypoint and a bool to create a new path between the current point and given waypoint
		//	if the ShouldInterpolate function returns true and our bool is true, we generate a new path between the points
		//	the generated path is then passed to FollowGenPath which functions different to FollowPath so certain things dont happen 
		//	like pen movement and regeneration interpolated paths
		
		double currentX = this.xAxis.getCurrentLocation();
		double currentZ = this.zAxis.getCurrentLocation();
		Waypoint currentLocal = new Waypoint(currentX, currentZ);
		
		shouldEnd();
		if(this.shouldInterpolate(currentLocal, point) && Interpolate) {
		
			shouldEnd();

			Path interpolatedPoints = this.generateInterpolatedPath(currentLocal, point);
			
			this.followGenPath(interpolatedPoints, false);
		}
		else {
			
			//	If we dont interpolate, we find the distance between the given point 
			//	and pass that into each axis Controller
			shouldEnd();
			int moveDistanceX = (int) (point.getX() - currentX);
			

			System.out.println("Moving " + moveDistanceX + "*");
			this.xAxis.goDegrees(moveDistanceX);
			

			shouldEnd();
			int moveDistanceZ = (int) (point.getY() - currentZ);
			System.out.println("Moving " + moveDistanceZ + "*");



			this.zAxis.goDegrees(moveDistanceZ);

		}
		
	}
	
	private void followGenPath(Path path, boolean shouldCalibrate) {
		
		//	The same fundimentals as followPath with certain behavious removed
		//	It will not use the putPenUp or putPenDown functions 
		//	and it purely calls to moveToPoint with no interpolating
		
		int pathSize = path.size();
		shouldEnd();
		for(int i = 0; i < pathSize; i++) {
			Waypoint nextPoint = path.get(i);
			shouldEnd();
			this.moveToPoint(nextPoint, false);
			if(shouldCalibrate && i % 5 == 0) {

				this.Recalibrate();
			}
		}
	}
	
	//	Follow Path	
	public void followPath(Path path, boolean shouldCalibrate) {
		
		//	has more behvious within this function to start a followPath correctly
		//	It will move to the starting point first then use putPenDown 
		//	then call to moveToPoint for each waypoint 
		//	Once complete it will call putPenUp ensuring its ready to draw another path
		
		// Move to start of Path
		
		if(!this.isValidPath(path)) {	//	if the path is invalid, we return and dont run the path 
			return;
		}
		
		if(this.isBatteryLow()) {	//	if battery low returns true, we return and don't run the path
			return;
		}
		shouldEnd();
		
		Waypoint startLocal = path.get(0);
		

		this.moveToPoint(startLocal, false);

		// Pen Down
		
		this.Pen.putPenDown();
		
		// Run Path
		
		int pathSize = path.size();


		for(int i = 1; i < pathSize; i++) {
			Waypoint nextPoint = path.get(i);
			System.out.println("Turn " + i);
			System.out.println("Going to " + nextPoint.x + ", " + nextPoint.y);

			shouldEnd();
			this.moveToPoint(nextPoint, true);
			
			if(shouldCalibrate && i % 5 == 0) {
			
				this.Recalibrate();
			}
		}
		
		// Pen Up
		
		this.Pen.putPenUp();

		
	}
	
	private boolean isValidPath(Path path) {
		
		//	Goes over every point within a path and make sure that 
		//	every point is within the boundries 
		//	if it isnt then the whole path is deemed invalid and won't run
		
		int xMax = this.xAxis.getAxisLength();
		int zMax = this.zAxis.getAxisLength();
		
		shouldEnd();
		for(int i = 0; i < path.size(); i++) {
			
			Waypoint point = path.get(i);
			
			if(point.getX() > xMax || point.getY() > zMax || point.getX() < 0 || point.getY() < 0)
				return false;
		}
		
		return true;
		
	}
	
	public boolean shouldInterpolate(Waypoint p1, Waypoint p2) {
		
		//	looks at the 2 points given and desides based 
		//	on distance and angle whether we should interpolate
		//	making sure that if a line is basically straight it wont create extra points slowing the robot down
		//	and if the points are basically next to each other it'll just work normally and not make more points
		
		double distance = p1.distance(p2.getX(), p2.getY());
		
		double gradient = Math.toDegrees(Math.asin(( p2.getX() - p1.getX() ) / ( p2.getY() - p1.getY() )));
		shouldEnd();
		if(distance <= this.xAxis.getAxisLength() * 0.05 || gradient % 90.0 <= 3.0 || gradient % 90 >= 86.0 ) {
			return false;
		}
		
		return true;
	}
	
	//	Generate Interpolated Path
	private Path generateInterpolatedPath(Waypoint p1, Waypoint p2) {
		Path returnPath = new Path();
		
		//	will use the Two given waypoints and an interpolation value to generate new points between them
		//	our interpolation value changes the size between each value and how many new points there will be
		//	out loop goes through using the "float counter" as a percentage of how far between the Two points we are 
		//	using the counter variable we generate a new point between the two points using the counter as a %
		//	of how far from the first one we are
		shouldEnd();
		float interpolationValue = 10f;
		double distance = p1.distance(p2.getX(), p2.getY());
		float counter = 0.0F;
		float x0 = (float) p1.getX();
		float y0 = (float) p1.getY();
		float x1 = (float) p2.getX();
		float y1 = (float) p2.getY();
		
		for(int i = 0; i < interpolationValue; i++) {
			float dt = (float) (distance * counter);
			float tRatio = (float) (dt / distance);
			shouldEnd();
			float xt = ((1-tRatio) * x0 + tRatio *x1);
			float yt = ((1-tRatio) * y0 + tRatio *y1);
			
			Waypoint newPoint = new Waypoint(xt,yt);
			returnPath.add(newPoint);
			counter += (interpolationValue)/100f;	
		}
		
		return returnPath;
	}
	
	private boolean isBatteryLow() {
		double lowBatteryThreshhold = 0.2;
		return (Battery.getVoltage() / 9.0) > lowBatteryThreshhold;
	}
	
	private void shouldEnd() {
		if(Button.ENTER.isDown()) {
			System.exit(0);
		}
	}
	
	//	Recalibrate
	private void Recalibrate() {

		//penUp
		
		//goToOrigin
		//goToPreviousLocation
		
		//penDown
	}
	
	
	public double getXScalar() {
		
		//	returns 1 or a number to make a point line up square on each axis
		
		double xLen = this.xAxis.getAxisLength();
		double yLen = this.zAxis.getAxisLength();
		
		if(xLen > yLen) {
			return xLen / yLen;
		}
		return 1;
	}
	
	public double getXLength() {
		return this.xAxis.getAxisLength();
	}
	
	
	
	public double getYScalar() {
		
		//	returns 1 or a number to make a point line up square on each axis
		
		double xLen = this.xAxis.getAxisLength();
		double yLen = this.zAxis.getAxisLength();
		
		if(yLen > xLen) {
			return yLen / xLen;
		}
		return 1;
	}
	
	public double getYLength() {
		return this.zAxis.getAxisLength();
	}
}