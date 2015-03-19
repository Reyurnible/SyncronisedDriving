package com.reyurnible.syncronizeddrivind.model.object;

/**
 * Created by shunhosaka on 2015/03/02.
 */
public class CarData {
    //ハンドル
    public int hundle = 0;
    //アクセル
    public boolean isAccel = false;
    //ブレーキ
    public int brake = 0;

    public CarData(int hundle, boolean isAccel, int brake) {
        this.hundle = hundle;
        this.isAccel = isAccel;
        this.brake = brake;
    }
}
