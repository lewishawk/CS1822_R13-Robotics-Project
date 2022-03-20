# CS1822_R13-Robotics-Project

<h1>PRINTER</h1>

---

Code For Robot:
- printer.java  : Inits everything, code to build off of
- axisInterface : Controls all the axis controllers and pen controller, what we use in printer.java to interact with the robot
- axisController: Controls a single axis allowing it to calibrate the axis and move the axis to any point on that axis
- penController : Controls the pen using a state machine to decide to move the pen up or down with calls from our axisInterface
