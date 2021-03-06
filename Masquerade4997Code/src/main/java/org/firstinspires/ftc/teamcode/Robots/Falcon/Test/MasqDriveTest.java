package org.firstinspires.ftc.teamcode.Robots.Falcon.Test;

/**
 * Created by Archishmaan Peyyety on 8/25/18.
 * Project: MasqLib
 */

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Robots.Falcon.Autonomus.Constants;
import org.firstinspires.ftc.teamcode.Robots.Falcon.Falcon;

import Library4997.MasqResources.MasqHelpers.Direction;
import Library4997.MasqWrappers.MasqLinearOpMode;

/**
 * Created by Archish on 2/7/18.
 */
@Autonomous(name = "MasqDriveTesting", group = "T")
@Disabled
public class MasqDriveTest extends MasqLinearOpMode implements Constants {
    private Falcon falcon = new Falcon();
    public void runLinearOpMode() throws InterruptedException {
        falcon.setStartOpenCV(false);
        falcon.mapHardware(hardwareMap);
        falcon.initializeAutonomous();
        falcon.driveTrain.setClosedLoop(true);
        while (!opModeIsActive()) {
            dash.create("lol");
            dash.update();
        }
        waitForStart();
        falcon.turnAbsolute(30, Direction.RIGHT);
        sleep(1);
    }
}