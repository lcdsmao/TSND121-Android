package jp.walkmate.tsndservice.Service;

/**
 * Created by Hirobe on 2015/11/02.
 */
public interface TSNDService {
    /**
     * 接続開始
     * @return
     * 接続されたときにtrue
     * 接続できなければfalse
     */
    boolean connect();

    boolean disconnect();

    /**
     * 計測開始
     */
    void run();

    /**
     * 計測停止
     */
    void stop();

    /**
     * ビープ音の音量を設定
     * @param param 0x00:消音,0x01:小,0x02:大
     */
    void setBeepVolume(byte[] param);

    /**
     * 音を鳴らす
     * @param param 1byteで音を指定
     */
    void beep(byte[] param);

    /**
     * X軸加速度取得
     * @return
     */
    int getAccX();

    /**
     * Y軸加速度取得
     * @return
     */
    int getAccY();

    /**
     * Z軸加速度取得
     * @return
     */
    int getAccZ();

    /**
     * X軸角速度取得
     * @return
     */
    int getGyrX();

    /**
     * Y軸角速度取得
     * @return
     */
    int getGyrY();

    /**
     * Z軸角速度取得
     * @return
     */
    int getGyrZ();

    int getMagX();

    int getMagY();

    int getMagZ();

    /**
     * センサ状態の取得
     * @return
     */
    SENSOR_STATUS getStatus();

    void cancelSensorThread();

    boolean isConnected();

    void clearBuffer();

    int getBatteryCharge();

    void setDeviceAddress(String address);

    enum SENSOR_STATUS{
        UNCONNECTED,
        USB_COMMAND,
        USB_MEASURING,
        BLUETOOTH_COMMAND,
        BLUETOOTH_MEASURING,
        UNKNOWN,
    };
}
