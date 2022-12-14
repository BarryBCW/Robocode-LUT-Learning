package RLRobot;

import robocode.*;
import robocode.RobocodeFileWriter;
import java.lang.Math;
import java.util.*;
import java.io.*;

public class LUT{

    private int myHP;
    private int enemyHP;
    private int distance;
    private int distanceWall;
    private int actionSize;
    private double[][][][][] LUT;
    private int[][][][][] visit;

    public LUT(int myHP, int enemyHP, int distance, int distanceWall, int action) {
        this.myHP = myHP;
        this.enemyHP = enemyHP;
        this.distance = distance;
        this.distanceWall = distanceWall;
        this.actionSize = action;
        LUT = new double[myHP][enemyHP][distance][distanceWall][action];
        visit = new int[myHP][enemyHP][distance][distanceWall][action];
        initialiseLUT();
    }
    public void initialiseLUT() {
        for(int i = 0; i < myHP; i++) {
            for(int j = 0; j < enemyHP; j++) {
                for(int k = 0; k < distance; k++) {
                    for(int m = 0; m < distanceWall; m++) {
                        for(int n = 0; n < actionSize; n++) {
                            LUT[i][j][k][m][n] = Math.random();
                            visit[i][j][k][m][n] = 0;
                        }
                    }
                }
            }
        }
    }
    public int getRandomAction() {
        Random random = new Random();
        return random.nextInt(actionSize);
    }

    public int getBestAction(int myHP, int enemyHP, int distance, int distanceWall) {
        double maxQ = -1;
        int actionIndex = -1;

        for(int i = 0; i < actionSize; i++) {
            if(LUT[myHP][enemyHP][distance][distanceWall][i] > maxQ) {
                actionIndex = i;
                maxQ = LUT[myHP][enemyHP][distance][distanceWall][i];
            }
        }
        return actionIndex;
    }
    public double getQValue(int myHP, int enemyHP, int distance, int distanceWall, int action) {
        return LUT[myHP][enemyHP][distance][distanceWall][action];
    }

    public void setQValue(int[] x, double argValue) {

        LUT[x[0]][x[1]][x[2]][x[3]][x[4]] = argValue;
        visit[x[0]][x[1]][x[2]][x[3]][x[4]]++;
    }
    public void save(File file) throws IOException {
        for(int i = 0; i < myHP; i++) {
            for(int j = 0; j < enemyHP; j++) {
                for(int k = 0; k < distance; k++) {
                    for(int m = 0; m < distanceWall; m++) {
                        for(int n = 0; n < actionSize; n++) {
                            String s = String.format("%d,%d,%d,%d,%d,%3f,%d", i,j, k, m, n, LUT[i][j][k][m][n],visit[i][j][k][m][n]);
                            RobocodeFileWriter fileWriter = new RobocodeFileWriter(file.getAbsolutePath(), true);
                            fileWriter.write(s + "\r\n");
                            fileWriter.close();
                        }
                    }
                }
            }
        }
    }

    public void load(File argFileName) throws IOException {
        FileInputStream inputFile = new FileInputStream(argFileName);
        BufferedReader inputReader = null;
        try {
            inputReader = new BufferedReader(new InputStreamReader(inputFile));
            for(int i = 0; i < myHP; i++) {
                for(int j = 0; j < enemyHP; j++) {
                    for(int k = 0; k < distance; k++) {
                        for(int m = 0; m < distanceWall; m++) {
                            for(int n = 0; n < actionSize; n++) {
                                String line = inputReader.readLine();
                                String [] args = line.split(",");
                                System.out.println(line);
                                double q = Double.parseDouble(args[5]);
                                LUT[i][j][k][m][n] = q;
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}