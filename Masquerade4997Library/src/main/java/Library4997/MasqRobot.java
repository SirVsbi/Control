package Library4997;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

import Library4997.MasqControlSystems.MasqIntegrator;
import Library4997.MasqControlSystems.MasqPurePursuit.MasqPath;
import Library4997.MasqControlSystems.MasqPurePursuit.MasqPoint;
import Library4997.MasqControlSystems.MasqPurePursuit.MasqPositionTracker;
import Library4997.MasqDriveTrains.MasqDriveTrain;
import Library4997.MasqResources.MasqHelpers.Direction;
import Library4997.MasqResources.MasqHelpers.StopCondition;
import Library4997.MasqResources.MasqPIDPackage;
import Library4997.MasqResources.MasqUtils;
import Library4997.MasqSensors.MasqClock;
import Library4997.MasqSensors.MasqColorSensor;
import Library4997.MasqWrappers.DashBoard;
import Library4997.MasqWrappers.MasqController;


/**
 * MasqRobot--> Contains all hardware and methods to runLinearOpMode the robot.
 */
public abstract class MasqRobot {
    public abstract void mapHardware(HardwareMap hardwareMap);
    public abstract MasqPIDPackage pidPackage();
    private Runnable
       turnFunction
            = new Runnable() {
                @Override
                public void run() {

                }
            },
       driveFunction
            = new Runnable() {
                @Override
                public void run() {

                }
            };
    public MasqDriveTrain driveTrain;
    public MasqPositionTracker tracker;
    public DashBoard dash;
    private MasqIntegrator pathOrientationError = new MasqIntegrator();
    private MasqClock timeoutClock = new MasqClock();
    public double angleLeftCover = 0;
    public static boolean opModeIsActive() {return MasqUtils.opModeIsActive();}
    public void drive(double distance, double speed, Direction direction, double timeOut, int sleepTime) {
        MasqClock timeoutTimer = new MasqClock();
        MasqClock loopTimer = new MasqClock();
        driveTrain.resetEncoders();
        double targetAngle = tracker.getHeading();
        double targetClicks = (int)(distance * driveTrain.getEncoder().getClicksPerInch());
        double clicksRemaining;
        double angularError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading()),
                prevAngularError = angularError, angularIntegral = 0,
                angularDerivative, powerAdjustment, power, leftPower, rightPower, maxPower, timeChange;
        do {
            clicksRemaining = (int) (targetClicks - Math.abs(driveTrain.getCurrentPosition()));
            power = ((clicksRemaining / targetClicks) * pidPackage().getKpDriveEncoder()) * direction.value * speed;
            power = Range.clip(power, -1.0, +1.0);
            timeChange = loopTimer.milliseconds();
            loopTimer.reset();
            angularError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
            angularIntegral = (angularIntegral + angularError) * timeChange;
            angularDerivative = (angularError - prevAngularError) / timeChange;
            prevAngularError = angularError;
            powerAdjustment = (pidPackage().getKpDriveAngular() * power) * angularError + (pidPackage().getKiDriveAngular() * angularIntegral) +
                    (pidPackage().getKdDriveAngular() * angularDerivative);
            powerAdjustment = Range.clip(powerAdjustment, -1.0, +1.0);
            powerAdjustment *= direction.value;
            leftPower = power - powerAdjustment;
            rightPower = power + powerAdjustment;
            maxPower = MasqUtils.max(Math.abs(leftPower), Math.abs(rightPower));
            if (maxPower > 1.0) {
                leftPower /= maxPower;
                rightPower /= maxPower;
            }
            driveTrain.setVelocity(leftPower, rightPower);
            driveFunction.run();
            dash.create("LEFT POWER: ",leftPower);
            dash.create("RIGHT POWER: ",rightPower);
            dash.create("ERROR: ", clicksRemaining);
            dash.update();
        } while (opModeIsActive() && !timeoutTimer.elapsedTime(timeOut, MasqClock.Resolution.SECONDS) && (clicksRemaining / targetClicks) > 0.05);
        driveTrain.stopDriving();
        sleep(sleepTime);
    }
    public void drive(double distance, double speed, Direction strafe, double timeOut) {
        drive(distance, speed, strafe, timeOut, MasqUtils.DEFAULT_SLEEP_TIME);
    }
    public void drive(double distance, double speed, Direction strafe) {
        drive(distance, speed, strafe, MasqUtils.DEFAULT_TIMEOUT);
    }
    public void drive(double distance, Direction direction, double timeout) {
        drive(distance, 0.5, direction, timeout);
    }
    public void drive(double distance, double speed){drive(distance, speed, Direction.FORWARD);}
    public void drive(double distance, Direction direction) {drive(distance, 0.5, direction);}
    public void drive(double distance) {drive(distance, 0.5);}

    public void driveAbsoluteAngle(double distance, int angle, double speed, Direction direction, double timeOut, int sleepTime) {
        MasqClock timeoutTimer = new MasqClock();
        MasqClock loopTimer = new MasqClock();
        driveTrain.resetEncoders();
        double targetClicks = (int)(distance * driveTrain.getEncoder().getClicksPerInch());
        double clicksRemaining;
        double angularError = tracker.imu.adjustAngle((double) angle - tracker.getHeading()),
                prevAngularError = angularError, angularIntegral = 0,
                angularDerivative, powerAdjustment, power, leftPower, rightPower, maxPower, timeChange;
        do {
            clicksRemaining = (int) (targetClicks - Math.abs(driveTrain.getCurrentPosition()));
            power = ((clicksRemaining / targetClicks) * pidPackage().getKpDriveEncoder()) * direction.value * speed;
            power = Range.clip(power, -1.0, +1.0);
            timeChange = loopTimer.milliseconds();
            loopTimer.reset();
            angularError = tracker.imu.adjustAngle((double) angle - tracker.getHeading());
            angularIntegral = (angularIntegral + angularError) * timeChange;
            angularDerivative = (angularError - prevAngularError) / timeChange;
            powerAdjustment = (pidPackage().getKpDriveAngular() * power) * angularError + (pidPackage().getKiDriveAngular() * angularIntegral) +
                    (pidPackage().getKdDriveAngular() * angularDerivative);
            powerAdjustment = Range.clip(powerAdjustment, -1.0, +1.0);
            powerAdjustment *= direction.value;
            leftPower = power - powerAdjustment;
            rightPower = power + powerAdjustment;
            maxPower = MasqUtils.max(Math.abs(leftPower), Math.abs(rightPower));
            if (maxPower > 1.0) {
                leftPower /= maxPower;
                rightPower /= maxPower;
            }
            driveTrain.setVelocity(leftPower, rightPower);
            driveFunction.run();
            //serializer.writeData(new Object[]{clicksRemaining, power, angularError, angularIntegral, angularDerivative, leftPower, rightPower, powerAdjustment});
            dash.create("LEFT POWER: ", leftPower);
            dash.create("RIGHT POWER: ", rightPower);
            dash.create("ERROR: ", clicksRemaining);
            dash.update();
            prevAngularError = angularError;
        } while (opModeIsActive() && !timeoutTimer.elapsedTime(timeOut, MasqClock.Resolution.SECONDS) && ((clicksRemaining / targetClicks) > 0.05));
        //serializer.close();
        driveTrain.stopDriving();
        sleep(sleepTime);
    }
    public void driveAbsoluteAngle(double distance, int angle, double speed, Direction strafe, double timeOut) {
        driveAbsoluteAngle(distance, angle, speed, strafe, timeOut, MasqUtils.DEFAULT_SLEEP_TIME);
    }
    public void driveAbsoluteAngle(double distance, int angle, double speed, Direction strafe) {
        driveAbsoluteAngle(distance, angle, speed, strafe, MasqUtils.DEFAULT_TIMEOUT);
    }
    public void driveAbsoluteAngle(double distance, int angle, double speed){driveAbsoluteAngle(distance, angle, speed, Direction.FORWARD);}
    public void driveAbsoluteAngle(double distance, int angle) {driveAbsoluteAngle(distance, angle, 0.5);}

    public void turnRelative(double angle, Direction direction, double timeOut, int sleepTime, double kp, double ki, double kd, boolean left, boolean right) {
        double targetAngle = tracker.imu.adjustAngle(tracker.getHeading()) + (direction.value * angle);
        double acceptableError = .5;
        double currentError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
        double prevError = 0;
        double integral = 0;
        double derivative;
        double newPower;
        double leftPower = 0, rightPower = 0;
        double previousTime = 0;
        timeoutClock.reset();
        while (opModeIsActive() && (tracker.imu.adjustAngle(Math.abs(currentError)) > acceptableError)
                && !timeoutClock.elapsedTime(timeOut, MasqClock.Resolution.SECONDS)) {
            double tChange = System.nanoTime() - previousTime;
            previousTime = System.nanoTime();
            tChange = tChange / 1e9;
            currentError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
            integral += currentError * tChange;
            derivative = (currentError - prevError) / tChange;
            double errorkp = currentError * kp;
            double integralki = integral * ki;
            double dervitivekd = derivative * kd;
            newPower = (errorkp + integralki + dervitivekd);
            if (Math.abs(newPower) >= 1) newPower /= Math.abs(newPower);
            if (left) leftPower = -newPower;
            if (right) rightPower = newPower;
            driveTrain.setVelocity(leftPower, rightPower);
            prevError = currentError;
            this.angleLeftCover = currentError;
            turnFunction.run();
            dash.create("TargetAngle", targetAngle);
            dash.create("Heading", tracker.getHeading());
            dash.create("AngleLeftToCover", currentError);
            dash.create("Power: ", newPower);
            dash.create("Raw Power: ", driveTrain.getPower());
            dash.update();
        }
        driveTrain.setVelocity(0,0);
        sleep(sleepTime);
    }
    public void turnRelative(double angle, Direction direction, double timeOut, int sleepTime, double kp, double ki) {
        turnRelative(angle, direction, timeOut, sleepTime, kp, ki, pidPackage().getKdTurn(), true, true);
    }
    public void turnRelative(double angle, Direction direction, double timeOut, int sleepTime, double kp) {
        turnRelative(angle, direction, timeOut, sleepTime, kp, pidPackage().getKiTurn());
    }
    public void turnRelative(double angle, Direction direction, double timeOut, int sleepTime) {
        turnRelative(angle, direction, timeOut, sleepTime,pidPackage().getKpTurn());
    }
    public void turnRelative(double angle, Direction direction, double timeout) {
        turnRelative(angle, direction, timeout, MasqUtils.DEFAULT_SLEEP_TIME);
    }
    public void turnRelative(double angle, Direction direction)  {
        turnRelative(angle, direction, MasqUtils.DEFAULT_TIMEOUT);
    }
    public void turnRelative(double angle, Direction direction, boolean left, boolean right)  {
        turnRelative(angle, direction, MasqUtils.DEFAULT_TIMEOUT, MasqUtils.DEFAULT_SLEEP_TIME,
                pidPackage().getKpTurn(), pidPackage().getKiTurn(), pidPackage().getKdTurn(), left, right);
    }

    public void turnAbsolute(double angle, Direction direction, double timeOut, int sleepTime, double kp, double ki, double kd) {
        double targetAngle = tracker.imu.adjustAngle((direction.value * angle));
        double acceptableError = .5;
        double currentError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
        double prevError = 0;
        double integral = 0;
        double derivative;
        double newPower;
        double previousTime = 0;
        timeoutClock.reset();
        while (opModeIsActive() && (tracker.imu.adjustAngle(Math.abs(currentError)) > acceptableError)
                && !timeoutClock.elapsedTime(timeOut, MasqClock.Resolution.SECONDS)) {
            double tChange = System.nanoTime() - previousTime;
            tChange = tChange / 1e9;
            currentError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
            integral += currentError * tChange;
            derivative = (currentError - prevError) / tChange;
            double p = currentError * kp;
            double i = integral * ki;
            double d = derivative * kd;
            newPower = p + i + d;
            if (Math.abs(newPower) >= 1) {newPower /= Math.abs(newPower);}
            driveTrain.setVelocity(-newPower, newPower);
            prevError = currentError;
            this.angleLeftCover = currentError;
            turnFunction.run();
            dash.create("KP: ", kp);
            dash.create("RIGHT POWER: " ,newPower);
            dash.create("TargetAngle", targetAngle);
            dash.create("Heading", tracker.getHeading());
            dash.create("AngleLeftToCover", currentError);
            dash.update();
            previousTime = System.nanoTime();
        }
        driveTrain.setVelocity(0,0);
        sleep(sleepTime);
    }
    public void turnAbsolute(double angle, Direction direction, double timeOut, int sleepTime, double kp, double ki) {
        turnAbsolute(angle, direction, timeOut, sleepTime, kp, ki, pidPackage().getKdTurn());
    }
    public void turnAbsolute(double angle, Direction direction, double timeOut, int sleepTime, double kp) {
        turnAbsolute(angle, direction, timeOut, sleepTime, kp, pidPackage().getKiTurn());
    }
    public void turnAbsolute(double angle, Direction direction, double timeOut, int sleepTime) {
        turnAbsolute(angle, direction, timeOut, sleepTime, pidPackage().getKpTurn());
    }
    public void turnAbsolute(double angle, Direction direction, double timeout)  {
        turnAbsolute(angle, direction, timeout, MasqUtils.DEFAULT_SLEEP_TIME);
    }
    public void turnAbsolute(double angle, Direction direction)  {
        turnAbsolute(angle, direction, MasqUtils.DEFAULT_TIMEOUT);
    }

    public void turnPOMAbsolute(double angle, Direction direction, double timeOut, int sleepTime, double kp, double ki, double kd) {
        double targetAngle = tracker.imu.adjustAngle((direction.value * angle));
        double acceptableError = .5;
        double startingAngle = tracker.imu.adjustAngle(tracker.getHeading());
        double currentError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
        double prevError = 0;
        double integral = 0;
        double derivative;
        double newPower;
        double previousTime = 0;
        timeoutClock.reset();
        while (opModeIsActive() && (tracker.imu.adjustAngle(Math.abs(currentError)) > acceptableError)
                && !timeoutClock.elapsedTime(timeOut, MasqClock.Resolution.SECONDS)) {
            double tChange = System.nanoTime() - previousTime;
            tChange = tChange / 1e9;
            currentError = tracker.imu.adjustAngle(targetAngle - tracker.getHeading());
            double currentAngle = tracker.imu.adjustAngle(tracker.getHeading());
            integral += currentError * tChange;
            derivative = (currentError - prevError) / tChange;
            double p = kp * (currentAngle - startingAngle);
            double i = integral * ki;
            double d = derivative * kd;
            newPower = p + i + d;
            if (Math.abs(newPower) >= 1) {newPower /= Math.abs(newPower);}
            driveTrain.setVelocity(-newPower, newPower);
            prevError = currentError;
            this.angleLeftCover = currentError;
            dash.create("KP: ", kp);
            dash.create("KI: ", ki);
            dash.create("RIGHT POWER: " , newPower);
            dash.create("TargetAngle", targetAngle);
            dash.create("Heading", tracker.getHeading());
            dash.create("AngleLeftToCover", currentError);
            dash.update();
            previousTime = System.nanoTime();
        }
        driveTrain.setVelocity(0,0);
        sleep(sleepTime);
    }
    public void turnPOMAbsolute(double angle, Direction direction, double timeOut, int sleepTime, double kp, double ki) {
        turnPOMAbsolute(angle, direction, timeOut, sleepTime, kp, ki, MasqUtils.KD.TURN_POM);
    }
    public void turnPOMAbsolute(double angle, Direction direction, double timeOut, int sleepTime, double kp) {
        turnPOMAbsolute(angle, direction, timeOut, sleepTime, kp, MasqUtils.KI.TURN_POM);
    }
    public void turnPOMAbsolute(double angle, Direction direction, double timeOut, int sleepTime) {
        turnPOMAbsolute(angle, direction, timeOut, sleepTime, MasqUtils.KP.TURN_POM);
    }
    public void turnPOMAbsolute(double angle, Direction direction, double timeout)  {
        turnPOMAbsolute(angle, direction, timeout, MasqUtils.DEFAULT_SLEEP_TIME);
    }
    public void turnPOMAbsolute(double angle, Direction direction)  {
        turnPOMAbsolute(angle, direction, MasqUtils.DEFAULT_TIMEOUT);
    }


    public void stopBlue(MasqColorSensor colorSensor, double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = tracker.getHeading();
        while ((!colorSensor.isBlue()) && opModeIsActive()){
            double newPower = power;
            double heading = tracker.getHeading();
            double error = targetAngle - heading;
            double errorkp = error *  pidPackage().getKpDriveAngular();
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setVelocityLeft(newPower * Direction.value);
            driveTrain.setVelocityRight(power * Direction.value);
            dash.create("Heading", heading);
            dash.create("Blue Val", colorSensor.colorNumber());
        }
        driveTrain.stopDriving();
    }
    public void stopBlue (MasqColorSensor colorSensor, double power){
        stopBlue(colorSensor, power, Direction.BACKWARD);
    }
    public void stopBlue (MasqColorSensor colorSensor) {stopBlue(colorSensor, 0.5);}

    public void stopRed(MasqColorSensor colorSensor, double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = tracker.getHeading();
        while (!(colorSensor.isRed()) && opModeIsActive()) {
            double newPower = power;
            double heading = tracker.getHeading();
            double error = targetAngle - heading;
            double errorkp = error * pidPackage().getKpDriveAngular();
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setVelocityLeft(newPower * Direction.value);
            driveTrain.setVelocityRight(power * Direction.value);
            dash.create("Heading", heading);
            dash.create("red Val", colorSensor.colorNumber());
        }
        driveTrain.stopDriving();
    }
    public void stopRed (MasqColorSensor colorSensor, double power){
        stopRed(colorSensor, power, Direction.BACKWARD);
    }
    public void stopRed (MasqColorSensor colorSensor) {stopRed(colorSensor, 0.5);}

    public void stop(StopCondition stopCondition, double angle, double speed, Direction direction, double timeOut) {
        MasqClock timeoutTimer = new MasqClock();
        MasqClock loopTimer = new MasqClock();
        driveTrain.resetEncoders();
        double angularError = tracker.imu.adjustAngle(angle - tracker.getHeading()),
                prevAngularError = angularError, angularIntegral = 0,
                angularDerivative, powerAdjustment, power, leftPower, rightPower, maxPower, timeChange;
        do {
            power = direction.value * speed;
            power = Range.clip(power, -1.0, +1.0);
            timeChange = loopTimer.milliseconds();
            loopTimer.reset();
            angularError = tracker.imu.adjustAngle(angle - tracker.getHeading());
            angularIntegral = (angularIntegral + angularError) * timeChange;
            angularDerivative = (angularError - prevAngularError) / timeChange;
            prevAngularError = angularError;
            powerAdjustment = (pidPackage().getKpDriveAngular() * power) * angularError + (pidPackage().getKiDriveAngular() * angularIntegral) +
                    (pidPackage().getKdDriveAngular() * angularDerivative);
            powerAdjustment = Range.clip(powerAdjustment, -1.0, +1.0);
            powerAdjustment *= direction.value;
            leftPower = power - powerAdjustment;
            rightPower = power + powerAdjustment;
            maxPower = MasqUtils.max(Math.abs(leftPower), Math.abs(rightPower));
            if (maxPower > 1.0) {
                leftPower /= maxPower;
                rightPower /= maxPower;
            }
            driveTrain.setVelocity(leftPower, rightPower);
            dash.create("LEFT POWER: ",leftPower);
            dash.create("RIGHT POWER: ",rightPower);
            dash.create("Angle Error", angularError);
            dash.update();
        } while (opModeIsActive() && !timeoutTimer.elapsedTime(timeOut, MasqClock.Resolution.SECONDS) && stopCondition.stop());
        driveTrain.stopDriving();
    }
    public void stop(StopCondition stopCondition, double angle, double speed, Direction direction) {
        stop(stopCondition, angle, speed, direction, MasqUtils.DEFAULT_TIMEOUT);
    }
    public void stop(StopCondition sensor, double angle, double power) {
        stop(sensor, angle, power, Direction.FORWARD);
    }
    public void stop(StopCondition stopCondition, double angle) {
        stop(stopCondition, angle, 0.5);
    }
    public void stop(StopCondition sensor){
        stop(sensor, tracker.getHeading());
    }
    public void stop(StopCondition stopCondition, int timeout) {
        stop(stopCondition, tracker.getHeading(), 0.5, Direction.FORWARD, timeout);
    }

    public void executePath (MasqPath path, Direction dir, double baseSpeed) {
        double direction = dir.value;
        double correction;
        double prevError = 0;
        double tChange, prevTime = 0;
        double error, integral, deriv;
        int wayPointIndex = 0;
        driveTrain.setClosedLoop(false);
        path.updateSystem(tracker.getPosition());
        for (MasqPoint point : path.getWayPoints()) {
            while (!point.equals(tracker.getPosition()) && opModeIsActive()) {
                tChange = System.nanoTime() - prevTime;
                tChange = tChange/1e9;
                error = path.getOrientationError(tracker.imu.getRelativeYaw(), tracker.getPosition());
                integral = pathOrientationError.getIntegral(error);
                deriv = (error - prevError) / tChange;
                correction = (error * MasqUtils.KP.PATH) + (integral * MasqUtils.KI.PATH) + (deriv * MasqUtils.KD.PATH);
                driveTrain.setPower((baseSpeed + correction) * direction, baseSpeed * direction);
                path.updateSystem(tracker.getPosition());
                prevTime = System.nanoTime();
                tracker.updateSystem();
                dash.create("Error: ", error);
                //dash.create("Goal: ", path.getOrientationError());
                dash.create("IMU: ", tracker.imu.getRelativeYaw());
                dash.update();
            }
            wayPointIndex++;
            if (wayPointIndex < path.getWayPoints().size())
            path.updatePath(path.getWayPoints().get(wayPointIndex - 1), path.getWayPoints().get(wayPointIndex));
        }
    }

    public void NFS(MasqController c) {
        float move = c.leftStickY();
        float turn = c.rightStickX();
        double left = move - turn;
        double right = move + turn;
        left *= -1;
        right *= -1;
        if (c.leftBumper()) {
            left /= 2;
            right /= 2;
        }
        if(left > 1.0) {
            left /= left;
            right /= left;
        }
        else if (right > 1.0) {
            left /= right;
            right /= right;
        }
        driveTrain.setVelocityLeft(left);
        driveTrain.setVelocityRight(right);
        //dash.create("POWER: ", driveTrain.getPower());
    }
    public void TANK(MasqController c) {
        double left = -c.leftStickY();
        double right = -c.rightStickY();
        double leftRate = driveTrain.leftDrive.getVelocity();
        double rightRate = driveTrain.rightDrive.getVelocity();
        double maxRate = MasqUtils.max(Math.abs(leftRate/left), Math.abs(rightRate/right));
        leftRate /= maxRate;
        rightRate /= maxRate;
        double leftError =  left - leftRate;
        double rightError = right - rightRate;
        driveTrain.rightDrive.setPower(right);
        driveTrain.leftDrive.setPower(left);
    }
    public void MECH(MasqController c, Direction direction, boolean disabled) {

        int disable = 1;
        if (disabled) disable = 0;
        double x = -c.leftStickY();
        double y = c.leftStickX();
        double xR = - c.rightStickX();
        double angle = Math.atan2(y, x);
        double adjustedAngle = angle + Math.PI/4;
        double speedMultiplier = 1.4;
        double turnMultiplier = 1.4;
        if (c.leftBumper()) xR = 0;
        double speedMagnitude = Math.hypot(x, y);
        double leftFront = (Math.sin(adjustedAngle) * speedMagnitude * speedMultiplier) - xR * turnMultiplier * direction.value;
        double leftBack = (Math.cos(adjustedAngle) * speedMagnitude * speedMultiplier) - xR  * turnMultiplier * direction.value;
        double rightFront = (Math.cos(adjustedAngle) * speedMagnitude * speedMultiplier) + xR * turnMultiplier * direction.value;
        double rightBack = (Math.sin(adjustedAngle) * speedMagnitude * speedMultiplier) + xR * turnMultiplier * direction.value;
        double max = MasqUtils.max(Math.abs(leftFront), Math.abs(leftBack), Math.abs(rightFront), Math.abs(rightBack));
        if (max > 1) {
            leftFront /= max;
            leftBack /= max;
            rightFront /= max;
            rightBack /= max;
        }
        if (c.leftTriggerPressed()) {
            leftFront /= 3;
            leftBack /= 3;
            rightFront /= 3;
            rightBack /= 3;
        }
        driveTrain.leftDrive.motor1.setVelocity(leftFront * direction.value * disable);
        driveTrain.leftDrive.motor2.setVelocity(leftBack  * direction.value * disable);
        driveTrain.rightDrive.motor1.setVelocity(rightFront * direction.value * disable);
        driveTrain.rightDrive.motor2.setVelocity(rightBack * direction.value * disable);
        dash.create("FRONT LEFT: ", driveTrain.leftDrive.motor1.getVelocity());
        dash.create("FRONT RIGHT: ", driveTrain.rightDrive.motor1.getVelocity());
        dash.create("BACK RIGHT: ", driveTrain.rightDrive.motor2.getVelocity());
        dash.create("BACK LEFT: ", driveTrain.leftDrive.motor2.getVelocity());
    }
    public void MECH(MasqController c, Direction direction) {
        MECH(c, direction, false);
    }
    public void MECH(MasqController c, boolean disabled) {
        MECH(c, Direction.FORWARD, disabled);
    }
    public void MECH(MasqController c) {
        MECH(c, Direction.FORWARD, false);
    }

    public void initializeTeleop(){
        driveTrain.setKp(pidPackage().getKpMotorTeleOp());
        driveTrain.setKi(pidPackage().getKiMotorTeleOp());
        driveTrain.setKd(pidPackage().getKdMotorTelOp());
    }
    public void initializeAutonomous(){
        driveTrain.setKp(pidPackage().getKpMotorAuto());
        driveTrain.setKi(pidPackage().getKiMotorAuto());
        driveTrain.setKd(pidPackage().getKdMotorAuto());
    }
    public void sleep(double timeSeconds) {
        try {
            Thread.sleep((long) timeSeconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void sleep() {sleep(MasqUtils.DEFAULT_SLEEP_TIME);}
    public WebcamName getWebCameName (HardwareMap hardwareMap, String name) {
        return hardwareMap.get(WebcamName.class, name);
    }

    public Runnable getTurnFunction() {
        return turnFunction;
    }

    public void setTurnFunction(Runnable turnFunction) {
        this.turnFunction = turnFunction;
    }

    public Runnable getDriveFunction() {
        return driveFunction;
    }

    public void setDriveFunction(Runnable driveFunction) {
        this.driveFunction = driveFunction;
    }
}
