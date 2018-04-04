package org.firstinspires.ftc.teamcode.Test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Autonomus.Constants;

import Library4997.MasqWrappers.MasqLinearOpMode;

/**
 * Created by Archish on 12/8/17.
 */
@TeleOp(name = "MasqServoProgrammer", group = "Autonomus")
public class MasqServoProgrammer extends MasqLinearOpMode implements Constants {
    double jewelArmBluePosition = 0, jewelArmRedPosition = 0,
            wristPosition = 0,
           rotatorRed = 0, rotatorBlue = 0, adjuster = 0, flipperLeft = 0, flipperRight = 0, redAdjuster = 0;
    public void runLinearOpMode() throws InterruptedException {
        robot.mapHardware(hardwareMap);
        while (!opModeIsActive()) {
            dash.create(robot.imu);
            dash.update();
        }
        waitForStart();
        robot.sleep(robot.getDelay());
        while (opModeIsActive()) {
            if  (controller2.bOnPress()) {
                adjuster += 0.01;
                controller2.update();
            }
            if (controller2.xOnPress()) {
                redAdjuster += 0.01;
                controller2.update();
            }
            if (controller2.yOnPress()) {
                redAdjuster -= 0.01;
                controller2.update();
            }
            if (controller2.aOnPress()) {
                adjuster -= 0.01;
                controller2.update();
            }
            if (controller2.leftTriggerOnPress()) {
                flipperLeft += 0.01;
                controller2.update();
            }
            if (controller2.leftBumperOnPress()) {
                flipperLeft -= 0.01;
                controller2.update();
            }
            if (controller2.rightTriggerOnPress()) {
                flipperRight += 0.01;
                controller2.update();
            }
            if (controller2.rightBumperOnPress()) {
                flipperRight -= 0.01;
                controller2.update();
            }
            if (controller1.aOnPress()) {
                jewelArmBluePosition += 0.01;
                controller1.update();
            }
            if (controller1.xOnPress()) {
                jewelArmBluePosition -= 0.01;
                controller1.update();
            }
            if (controller1.bOnPress()) {
                wristPosition += 0.01;
                controller1.update();
            }
            if (controller1.yOnPress()) {
                wristPosition -= 0.01;
                controller1.update();
            }
            if (controller1.rightBumperOnPress()) {
                rotatorRed += 0.01;
                controller1.update();
            }
            if (controller1.rightTriggerOnPress()) {
                rotatorRed -= 0.01;
                controller1.update();
            }
            if (controller1.leftBumperOnPress()) {
                rotatorBlue += 0.01;
                controller1.update();
            }
            if (controller1.leftTriggerOnPress()) {
                rotatorBlue -= 0.01;
                controller1.update();
            }
            robot.redRotator.setPosition(rotatorRed);
            robot.flipper.gripTop.setPosition(adjuster);
            robot.flipper.gripBottom.setPosition(redAdjuster);
            robot.flipper.flipperLeft.setPosition(flipperLeft);
            robot.flipper.flipperRight.setPosition(flipperRight);
            robot.relicAdjuster.setPosition(jewelArmRedPosition);
            dash.create("FLIP RIGHT: ", flipperRight);
            dash.create("FLIP LEFT: ", flipperLeft);
            dash.create("GRIP TOP: ", adjuster);
            dash.create("GRIP BOTTOM: ", redAdjuster);
            dash.create("JEWEL ARM RED: ", jewelArmRedPosition);
            dash.create("JEWEL ARM BLUE: ", jewelArmBluePosition);
            dash.create("REDR: ", rotatorRed);
            dash.create("BLUER: ", rotatorBlue);
            dash.update();
            controller1.update();
            controller2.update();
            robot.sleep(50);
        }
    }
}