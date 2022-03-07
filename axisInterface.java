

public class axisInterface{
	
	private axisController xAxis;
	private axisController yAxis;
	//private penController  zPen;
	
	public axisInterface(axisController xAxis, axisController yAxis) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
	}
	
	
	//	The Motors Need to move in time with each other therefore we need a way to get and sync the motors at the same time
	//	We could use the inbuild sync functions on the motors to do this therefore ever movement update would run together
	//	This means that in here we can use STATES for the printer IE: DownMoving, UpMoving, Stopped
	//	Then using a Current XY and Next XY we can move them all at the same time and make it more smoothly
	//	
	//	So we will do it like this :
	//	
	//	xAxis.setSync(new BaseRegulatedMotor { yAxis.getMotor() });
	//	xAxis.startSync();
	//	
	//	...
	//
	//	xAxis.goDegree(30);			Do Movements Here
	//	yAxis.goDegree(30);			Do Movements Here
	//
	//	...
	//
	//	xAxis.endSync();
	//
	//	xAxis.getMotor().waitComplete();
	//	yAxis.getMotor().waitComplete();
	
}