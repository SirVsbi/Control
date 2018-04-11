package Library4997.MasqMotors;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.Arrays;
import java.util.List;

import Library4997.MasqUtilities.MasqHardware;
import Library4997.MasqUtilities.MasqUtils;
import Library4997.MasqUtilities.PID_CONSTANTS;

import static Library4997.MasqUtilities.MasqUtils.opModeIsActive;

/**
 * MasqMotorSystem That supports two or more motors and treats them as one
 */
public class MasqMotorSystem implements PID_CONSTANTS, MasqHardware {
    public MasqMotor motor1 , motor2, motor3;
    private List<MasqMotor> motors;
    private int numMotors;
    private boolean stalled, stallDetection;
    private Runnable stallAction, unStalledAction;
    private double slowDown = 0;
    private String systemName;
    public MasqMotorSystem(String name1, DcMotor.Direction direction, String name2, DcMotor.Direction direction2, String systemName, HardwareMap hardwareMap) {
        this.systemName = systemName;
        motor1 = new MasqMotor(name1, direction, hardwareMap);
        motor2 = new MasqMotor(name2, direction2, hardwareMap);
        motor3 = null;
        motors = Arrays.asList(motor1, motor2);
        numMotors = 2;
    }
    public MasqMotorSystem(String name1, String name2, String systemName, HardwareMap hardwareMap) {
        this.systemName = systemName;
        motor1 = new MasqMotor(name1, DcMotor.Direction.FORWARD, hardwareMap);
        motor2 = new MasqMotor(name2, DcMotor.Direction.FORWARD, hardwareMap);
        motor3 = null;
        motors = Arrays.asList(motor1, motor2);
        numMotors = 2;
    }
    public MasqMotorSystem(String name1, DcMotor.Direction direction,
                           String name2, DcMotor.Direction direction2,
                           String name3, DcMotor.Direction direction3, String systemName,
                            HardwareMap hardwareMap) {
        this.systemName = systemName;
        motor1 = new MasqMotor(name1, direction, hardwareMap);
        motor2 = new MasqMotor(name2, direction2, hardwareMap);
        motor3 = new MasqMotor(name3, direction3, hardwareMap);
        motors = Arrays.asList(motor1, motor2, motor3);
        numMotors = 3;
    }
    public MasqMotorSystem(String name1, String name2, String name3, String systemName, HardwareMap hardwareMap) {
        this.systemName = systemName;
        motor1 = new MasqMotor(name1, DcMotor.Direction.FORWARD, hardwareMap);
        motor2 = new MasqMotor(name2, DcMotor.Direction.FORWARD, hardwareMap);
        motor3 = new MasqMotor(name3, DcMotor.Direction.FORWARD, hardwareMap);
        motors = Arrays.asList(motor1, motor2, motor3);
        numMotors = 3;
    }
    public MasqMotorSystem resetEncoder () {
        for (MasqMotor masqMotor : motors)
            masqMotor.resetEncoder();
        return this;
    }
    public MasqMotorSystem setEncoderCounts(double counts) {
        for (MasqMotor masqMotor: motors) masqMotor.setEncoderCounts(counts);
        return this;
    }
    public MasqMotorSystem setKp(double kp){
        for (MasqMotor masqMotor: motors) masqMotor.setKp(kp);
        return this;
    }
    public MasqMotorSystem setKi(double ki){
        for (MasqMotor masqMotor: motors) masqMotor.setKi(ki);
        return this;
    }
    public double getPower() {
        double num = 0, sum = 0;
        for (MasqMotor masqMotor: motors) {
            sum += masqMotor.currentPower;
            num++;
        }
        return sum/num;
    }
    public MasqMotorSystem setKd(double kd){
        for (MasqMotor masqMotor: motors) masqMotor.setKd(kd);
        return this;
    }
    public void setSlowDown(double slowDown) {this.slowDown = slowDown;}
    public void setPower (double power) {
        int index = 1;
        for (MasqMotor masqMotor : motors){
            if (index == 2) power -= slowDown;
            masqMotor.setPower(power);
            index++;
        }
    }
    public MasqMotorSystem setDistance(int distance){
        for (MasqMotor masqMotor: motors)
            masqMotor.setDistance(distance);
        return this;
    }
    public MasqMotorSystem runUsingEncoder() {
        for (MasqMotor masqMotor: motors)
            masqMotor.runUsingEncoder();
        return this;
    }
    public MasqMotorSystem breakMotors(){
        for (MasqMotor masqMotor: motors)
            masqMotor.setBreakMode();
        return this;
    }
    public MasqMotorSystem runWithoutEncoders() {
        for (MasqMotor masqMotor: motors)
            masqMotor.runWithoutEncoders();
        return this;
    }
    public MasqMotorSystem setClosedLoop (boolean closedLoop){
        for (MasqMotor masqMotor: motors)
            masqMotor.setClosedLoop(closedLoop);
        return this;
    }
    public double getVelocity(){
        double i = 0;
        double rate = 0;
        for (MasqMotor masqMotor: motors){
            rate += masqMotor.getVelocity();
            i++;
        }
        return rate/i;
    }
    public double getAcceleration(){
        double i = 0;
        double rate = 0;
        for (MasqMotor masqMotor: motors){
            rate += masqMotor.getAcceleration();
            i++;
        }
        return rate/i;
    }
    public void stopDriving() {
        setPower(0);
    }
    public static int convert(int TICKS) {
        return (int) ((TICKS * 35.1070765836));
    }
    public boolean isBusy() {
        boolean isBusy = false;
        for (MasqMotor masqMotor: motors)
            isBusy = masqMotor.isBusy();
        return isBusy;
    }
    public double getCurrentPosition() {
        int total = 0;
        for (MasqMotor m : motors) total += m.getCurrentPosition();
        return total / numMotors;
    }

    private synchronized boolean getStalled() {
        return Math.abs(getVelocity()) < 10;
    }

    public void setStalledAction(Runnable action) {
        stallAction = action;
    }
    public void setUnStalledAction(Runnable action) {
        unStalledAction = action;
    }
    public void setStallDetection(boolean stallDetection) {
        this.stallDetection = stallDetection;}
    private boolean getStallDetection () {return stallDetection;}
    public void enableStallDetection() {
        stallDetection = true;
        Runnable mainRunnable = new Runnable() {
            @Override
            public void run() {
                while (opModeIsActive()) {
                    stalled = getStalled();
                    if (getStallDetection()) {
                        if (stalled) stallAction.run();
                        else unStalledAction.run();
                    }
                    MasqUtils.sleep(100);
                }
            }
        };
        Thread thread = new Thread(mainRunnable);
        thread.start();
    }

    public String getName() {
        return systemName;
    }
    public String[] getDash() {
        return new String[]{ "Current Position" + getCurrentPosition()};
    }
}