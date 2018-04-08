package org.firstinspires.ftc.teamcode.Autonomus;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import Library4997.MasqSensors.MasqClock;
import Library4997.MasqUtilities.Direction;
import Library4997.MasqUtilities.StopCondition;
import Library4997.MasqWrappers.MasqLinearOpMode;
import SubSystems4997.SubSystems.Flipper.Position;
import SubSystems4997.SubSystems.Gripper.Grip;

;

/**
 * Created by Archish on 3/25/18.
 */
@Autonomous(name = "RedAuto", group = "Autonomus")
public class RedAuto extends MasqLinearOpMode implements Constants {
    public void runLinearOpMode() throws InterruptedException {
        final MasqClock clock = new MasqClock();
        robot.mapHardware(hardwareMap);
        robot.initializeAutonomous();
        robot.initializeServos();
        while (!opModeIsActive()) {
            dash.create(robot.imu);
            dash.update();
        }
        waitForStart();
        robot.sleep(robot.getDelay());
        robot.intake.setPower(INTAKE);
        robot.gripper.setGripperPosition(Grip.CLAMP);
        robot.drive(28, POWER_LOW, Direction.BACKWARD);
        robot.flipper.flip(0);
        robot.turnAbsolute(45, Direction.RIGHT);
        robot.drive(5, POWER_OPTIMAL, Direction.BACKWARD);
        robot.gripper.setGripperPosition(Grip.OUT);
        robot.drive(5);
        robot.flipper.setFlipperPosition(Position.IN);
        robot.turnAbsolute(90, Direction.RIGHT);
        clock.reset();
        robot.driveAbsolute(new StopCondition() {
            @Override
            public boolean stop() {
                return robot.doubleBlock.stop();
            }
        }, 60, -90, POWER_OPTIMAL, Direction.FORWARD);
        robot.gripper.setGripperPosition(Grip.CLAMP);
        robot.flipper.flip(0);
        robot.driveAbsoluteAngle(30, -85, POWER_HIGH, Direction.BACKWARD);
    }
}