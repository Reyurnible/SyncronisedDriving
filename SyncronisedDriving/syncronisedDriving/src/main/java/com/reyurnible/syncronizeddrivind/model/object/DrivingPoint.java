package com.reyurnible.syncronizeddrivind.model.object;

/**
 * Created by shunhosaka on 2015/03/02.
 */
public class DrivingPoint {
    //ハンドル
    public int hundleTotal = 0;
    public int hundle = 0;
    //アクセル
    public int accelTotal = 0;
    public int accel = 0;
    //ブレーキ
    public int brakeTotal = 0;
    public int brake = 0;

    public DrivingPoint(int hundleTotal, int hundle, int accelTotal, int accel, int brakeTotal, int brake) {
        this.hundleTotal = hundleTotal;
        this.hundle = hundle;
        this.accelTotal = accelTotal;
        this.accel = accel;
        this.brakeTotal = brakeTotal;
        this.brake = brake;
    }

    public void initialize() {
        hundleTotal = 0;
        hundle = 0;
        accelTotal = 0;
        accel = 0;
        brakeTotal = 0;
        brake = 0;
    }

    public int getHundleSyncro() {
        if (hundleTotal == 0) {
            return 100;
        } else {
            int syncro = (int) ((float) hundle / hundleTotal * 100);
            if (syncro < 1) {
                hundle = 0;
                hundleTotal = 0;
            }
            return syncro;
        }
    }

    public int getAccelSyncro() {
        if (accelTotal == 0) {
            return 100;
        } else {
            int syncro = (int) ((float) accel / accelTotal * 100);
            if (syncro < 1) {
                accel = 0;
                accelTotal = 0;
            }
            return syncro;
        }
    }

    public int getBrakeSyncro() {
        if (brakeTotal == 0) {
            return 100;
        } else {
            int syncro = (int) ((float) brake / brakeTotal * 100);
            if (syncro < 1) {
                brake++;
                brakeTotal++;
            }
            return syncro;
        }
    }

}
