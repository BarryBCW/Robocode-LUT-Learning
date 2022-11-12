package RLRobot;

import robocode.*;

import java.awt.*;
import java.io.File;

import robocode.RobocodeFileWriter;
import java.io.*;
public class RLRobot extends AdvancedRobot {

    //Enum type
    public enum HP {low, medium, high};
    public enum Distance {close, medium, far};
    public enum Action {fire, left, right, forward, backward};
    public enum operaMode {onScan, onAction};
    private HP curMyHP = HP.high;
    private HP curEneHP = HP.high;
    private Distance curMyDistance = Distance.close;
    private Distance curWaDistance = Distance.far;
    private Action curAction = Action.forward;

    private HP preMyHP = HP.high;
    private HP preEneHP = HP.high;
    private Distance preMyDistance = Distance.close;
    private Distance preWaDistance = Distance.far;
    private Action preAction = Action.forward;

    private operaMode Mode= operaMode.onScan;

    public double myX = 0.0;
    public double myY = 0.0;
    public double myHP = 100;
    public double enemyHP = 100;
    public double dis = 0.0;



    public static boolean loadTrue = true;

    public static boolean onPolicy = false;

    private double gamma = 0.9;
    // Learning rate
    private double alpha = 0.1;
    private double epsilon = 0;
    // Q
    private double Q = 0.0;
    // Reward
    private double reward = 0.0;

    private final double immediateBonus = 0.5;
    private final double terminalBonus = 1.0;
    private final double immediatePenalty = -0.2;
    private final double terminalPenalty = -0.4;

    public static int curActionIndex;
    public static double enemyBearing;

    public static int totalRound = 0;
    public static int round = 0;
    public static int winRound = 0;
//    public static double[] winPercentage = new double[351];
    public static double winPercentage = 0.0;
    public static String fileToSaveLUT = RLRobot.class.getSimpleName() + "LUT";
    public static LUT lut = new LUT(HP.values().length,
            HP.values().length,
            Distance.values().length,
            Distance.values().length,
            Action.values().length);
    public void writeTo(File fileToWrite, double winRate, int roundCount) throws IOException {


            RobocodeFileWriter fileWriter = new RobocodeFileWriter(fileToWrite.getAbsolutePath(), true);
            fileWriter.write(roundCount + " " + Double.toString(winRate) + "\r\n");
            fileWriter.close();


    }

    public HP getHPLevel(double hp) {
        HP remain = null;
        if(hp < 0) {
            return null;
        } else if(hp <= 33) {
            remain = HP.low;
        } else if(hp <= 67) {
            remain = HP.medium;
        } else {
            remain = HP.high;
        }
        return remain;
    }

    // Get the distance
    public Distance getDistanceFromWall(double x1, double y1) {
        Distance distance_Wall = null;
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();
        double distance_up = height - y1;

        double distance_right = width - x1;
        if(y1 < 30 || distance_up < 30 || x1 < 30 || distance_right < 30) {
            distance_Wall = Distance.close;
        } else if(y1 < 80 || distance_up < 80 || x1 < 80 || distance_right < 80) {
            distance_Wall = Distance.medium;
        } else {
            distance_Wall = Distance.far;
        }
        return distance_Wall;
    }
    // Get the distance level
    public Distance getDistanceLevel(double distance) {
        Distance remain = null;
        if(distance < 0) {
            return null;
        } else if(distance < 333) {
            remain = Distance.close;
        } else if(distance < 667) {
            remain = Distance.medium;
        } else {
            remain = Distance.far;
        }
        return remain;
    }

    public double calQ(double reward, boolean onPolicy) {
        double previousQ = lut.getQValue(
                preMyHP.ordinal(),
                preEneHP.ordinal(),
                preMyDistance.ordinal(),
                preWaDistance.ordinal(),
                preAction.ordinal()
        );

        double curQ = lut.getQValue(
                curMyHP.ordinal(),
                curEneHP.ordinal(),
                curMyDistance.ordinal(),
                curWaDistance.ordinal(),
                curAction.ordinal()
        );

        int bestActionIndex = lut.getBestAction(
                curMyHP.ordinal(),
                curEneHP.ordinal(),
                curMyDistance.ordinal(),
                curWaDistance.ordinal()
        );

        // Get the maximum Q ( Off-policy )
        double maxQ = lut.getQValue(
                curMyHP.ordinal(),
                curEneHP.ordinal(),
                curMyDistance.ordinal(),
                curWaDistance.ordinal(),
                bestActionIndex
        );

        double res =  onPolicy ?
                previousQ + alpha * (reward + gamma * curQ - previousQ) :
                previousQ + alpha * (reward + gamma * maxQ - previousQ);
        return res;
    }
    @Override
    public void run() {
        if (loadTrue) {
            //loadTable();
        }
        loadTrue = false;
        super.run();
        setBulletColor(Color.blue);
        setGunColor(Color.black);
        setBodyColor(Color.yellow);
        setRadarColor(Color.white);
        curMyHP = HP.high;
        while (true) {
            switch (Mode) {
                case onScan: {
                    reward = 0.0;
                    turnRadarLeft(90);
                    break;
                }
                case onAction: {
                    curMyDistance = getDistanceFromWall(myX, myY);

                    curActionIndex = (Math.random() <= epsilon)
                            ? lut.getRandomAction() // explore a random action
                            : lut.getBestAction(
                            getHPLevel(myHP).ordinal(),
                            getHPLevel(enemyHP).ordinal(),
                            getDistanceLevel(dis).ordinal(),
                            curMyDistance.ordinal()); // select greedy action
                    curAction = Action.values()[curActionIndex];
                    switch (curAction) {
                        case fire: {
                            turnGunRight(getHeading() - getGunHeading() + enemyBearing);
                            fire(3);
                            break;
                        }
                        case left: {
                            setTurnLeft(45);
                            execute();
                            break;
                        }

                        case right: {
                            setTurnRight(45);
                            execute();
                            break;
                        }

                        case forward: {
                            setAhead(100);
                            execute();
                            break;
                        }
                        case backward: {
                            setBack(100);
                            execute();
                            break;
                        }
                    }
                    int[] indexes = new int[]{
                            preMyHP.ordinal(),
                            preEneHP.ordinal(),
                            preMyDistance.ordinal(),
                            preWaDistance.ordinal(),
                            preAction.ordinal()
                    };
                    Q = calQ(reward, onPolicy);
                    lut.setQValue(indexes, Q);
                    Mode = operaMode.onScan;
        }

            }
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        super.onScannedRobot(e);
        enemyBearing = e.getBearing();

        myX = getX();
        myY = getY();
        myHP = getEnergy();
        enemyHP = e.getEnergy();
        dis = e.getDistance();

        preMyHP = curMyHP;
        preEneHP = curEneHP;
        preMyDistance = curMyDistance;
        preWaDistance = curWaDistance;
        preAction = curAction;

        curMyHP = getHPLevel(myHP);
        curEneHP = getHPLevel(enemyHP);
        curMyDistance = getDistanceLevel(dis);
        curWaDistance = getDistanceFromWall(myX, myY);
        Mode = operaMode.onAction;

    }

    @Override
    public void onHitByBullet(HitByBulletEvent e){

            reward += immediatePenalty;

    }

    @Override
    public void onBulletHit(BulletHitEvent e){

            reward += immediateBonus;

    }

    @Override
    public void onBulletMissed(BulletMissedEvent e){

            reward += immediatePenalty;

    }

    @Override
    public void onHitWall(HitWallEvent e){
        avidObstacle();
    }
    public void avidObstacle() {
        setBack(150);
        setTurnRight(60);
        execute();
    }
    @Override
    public void onHitRobot(HitRobotEvent e) {
            reward += immediatePenalty;

        avidObstacle();
    }
    @Override
    public void onWin(WinEvent e){
        reward = terminalBonus;
        int[] indexes = new int []{
                preMyHP.ordinal(),
                preEneHP.ordinal(),
                preMyDistance.ordinal(),
                preWaDistance.ordinal(),
                preAction.ordinal()};
        Q = calQ(reward, onPolicy);
        lut.setQValue(indexes, Q);
        winRound++;
        totalRound++;

        if((totalRound % 100 == 0) && (totalRound != 0)){
            winPercentage = (double) winRound / 100;
            ++round;
            File folderDst1 = getDataFile( "winningRate.log");
            try {
                writeTo(folderDst1, winPercentage, round);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            winRound = 0;

        }
        if ((totalRound % 4000 == 0) && (totalRound != 0)){
            //saveTable();
        }

    }

    @Override
    public void onDeath(DeathEvent e){


        reward = terminalPenalty;
        int[] indexes = new int []{
                preMyHP.ordinal(),
                preEneHP.ordinal(),
                preMyDistance.ordinal(),
                preWaDistance.ordinal(),
                preAction.ordinal()};
        Q = calQ(reward, onPolicy);
        lut.setQValue(indexes, Q);
        totalRound++;

        if((totalRound % 100 == 0) && (totalRound != 0)){

            winPercentage = (double) winRound / 100;
            ++round;
            File folderDst1 = getDataFile( "winningRate.log");
            try {
                writeTo(folderDst1, winPercentage, round);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            winRound = 0;
        }
        if ((totalRound % 4000 == 0) && (totalRound != 0)){
            //saveTable();
        }
    }
    public void saveTable() {
        try {
            String file = fileToSaveLUT + "-" + epsilon + ".log";
            lut.save(getDataFile(file));
        } catch (Exception e) {
            System.out.println("Save Error!" + e);
        }
    }

    public void loadTable() {
        try {
            String file = fileToSaveLUT + "-0.7.log";
            lut.load(getDataFile(file));
        } catch (Exception e) {
            System.out.println("Save Error!" + e);
        }
    }

}
