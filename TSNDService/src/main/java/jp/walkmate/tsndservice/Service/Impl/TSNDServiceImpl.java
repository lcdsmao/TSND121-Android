package jp.walkmate.tsndservice.Service.Impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.walkmate.tsndservice.Service.TSNDService;

/**
 * Created by Hirobe on 2015/11/02.
 */
public class TSNDServiceImpl implements TSNDService {
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final byte PROTOCOL_HEADER = (byte)0x9A;
    public static final byte COMMAND_START_MEASURING = (byte)0x13;
    public static final byte COMMAND_STOP_MEASURING = (byte)0x15;
    public static final byte COMMAND_ACCGYR_SETTING  = (byte)0x16;
    public static final byte COMMAND_MAG_SETTING = (byte)0x18;
    public static final byte COMMAND_ACC_RANGE_SETTING  = (byte)0x22;
    public static final byte COMMAND_GYR_RANGE_SETTING  = (byte)0x25;
    public static final byte COMMAND_MAG_CALIBRATION_SETTING = (byte)0x28;
    public static final byte COMMAND_SET_BEEP_VOLUME = (byte)0x32;
    public static final byte COMMAND_SOUND_BEEP  = (byte)0x34;
    public static final byte COMMAND_GET_BATTERY_CHARGE = (byte)0x3B;
    public static final byte COMMAND_GET_STATUS = (byte)0x3C;


    public static final byte RECEIVED_ACC_GYRO_DATA = (byte)0x80;
    public static final byte RECEIVED_MAG_DATA = (byte)0x81;
    public static final byte RECEIVED_START_MEASURING = (byte)0x93;
    public static final byte RECEIVED_GET_BATTERY_CHARGE = (byte)0xBB;
    public static final byte RECEIVED_GET_STATUS = (byte)0xBC;

    protected int sampling_interval_msec = 10; // >10 because of mag?

    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;

    protected String deviceAddress = "00:07:80:76:8F:60";
    protected String deviceName = "";

    protected BluetoothDevice device = null;
    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothSocket bluetoothSocket = null;

    protected Timer measuringTimer  = null;

    protected int time;
    protected int acc_x, acc_y, acc_z;
    protected int gyr_x, gyr_y, gyr_z;
    protected int mag_x, mag_y, mag_z;

    protected boolean isConnected = false;

    protected byte[] buffer = new byte[1024];
    protected byte[] byteBuffer = new byte[1];
    protected boolean isClearBuffering = false;

    protected byte[] received_params = new byte[32];

    protected int waitCount = 0;
    protected int received_length;

    int bcc;

    public TSNDServiceImpl(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public TSNDServiceImpl(String address){
        deviceAddress = new String(address);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public TSNDServiceImpl(BluetoothAdapter adapter){
        bluetoothAdapter = adapter;
    }

    public TSNDServiceImpl(String address, BluetoothAdapter adapter){
        deviceAddress = new String(address);
        bluetoothAdapter = adapter;
    }

    public TSNDServiceImpl(String address, String deviceName, BluetoothAdapter adapter){
        this(address, adapter);
        this.deviceName = new String(deviceName);
    }

    public TSNDServiceImpl(String address, String deviceName, BluetoothAdapter adapter, int interval){
        this(address, deviceName, adapter);
        sampling_interval_msec = interval;
    }

    @Override
    public boolean connect(){
        isConnected = false;

        try {
            device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        } catch (IllegalArgumentException e) {
            Log.d("ERROR", "MACアドレス不正");
            return false;
        }

        try {
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            bluetoothSocket.connect();

            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

            for(int i=0; i<2; i++) {
                byte[] param = {0x02};
                beep(param);
            }

            isConnected = true;
        }
        catch (IOException e) {
            Log.d("ERROR", "Bluetooth Socket IO");
            return false;
        }
        catch (Exception e) {
            Log.d("ERROR", "Bluetooth Socket");
            return false;
        }

        return true;
    }

    @Override
    public boolean disconnect(){
        byte[] param = {0x07};
        beep(param);
        try {
            bluetoothSocket.close();
            isConnected = false;
            return true;
        }catch (IOException e){

        }

        return false;
    }

    @Override
    public void run(){
        isConnected = true;

        initSetting();
        startMeasuring();

        measuringTimer = new Timer(true);
        measuringTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                if(!isClearBuffering) {
                    if (!getSensorData()) {
                        this.cancel();
                    }
                }
            }
        }, 0, (int)(sampling_interval_msec*0.25));
    }

    @Override
    public void stop(){
        if(measuringTimer != null) {
            measuringTimer.cancel();
        }
        stopMeasuring();
    }

    @Override
    public void setBeepVolume(byte[] param){
        sendCommand(COMMAND_SET_BEEP_VOLUME, param);
        try {
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void beep(byte[] param){
        sendCommand(COMMAND_SOUND_BEEP, param);
        try {
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SENSOR_STATUS getStatus(){
        byte[] param = {0x00};

        sendCommand(COMMAND_GET_STATUS, param);
        try{
            int count = 0;
            while(count < 100000) {
                if (-1 != inputStream.read(byteBuffer)) {
                    if (PROTOCOL_HEADER == byteBuffer[0]) {
                        inputStream.read(byteBuffer);
                        if (RECEIVED_GET_STATUS == byteBuffer[0]) {
                            inputStream.read(byteBuffer);

                            int status = byteBuffer[0];
                            switch (status) {
                                case 0:
                                    return SENSOR_STATUS.USB_COMMAND;
                                case 1:
                                    return SENSOR_STATUS.USB_MEASURING;
                                case 2:
                                    return SENSOR_STATUS.BLUETOOTH_COMMAND;
                                case 3:
                                    return SENSOR_STATUS.BLUETOOTH_MEASURING;
                            }

                            return SENSOR_STATUS.UNKNOWN;
                        }
                    }
                }
                count++;
            }

        }catch (IOException e){
            Log.d("DeviceInfo", e.toString());
        }

        this.disconnect();
        isConnected = false;
        return SENSOR_STATUS.UNCONNECTED;
    }

    @Override
    public int getBatteryCharge(){
        byte[] param = {0x00};

        sendCommand(COMMAND_GET_BATTERY_CHARGE, param);

        try {
            int count=0;
            while(count < 1000) {
                if (-1 != inputStream.read(byteBuffer)) {
                    if (PROTOCOL_HEADER == byteBuffer[0]) {
                        inputStream.read(byteBuffer);
                        if (RECEIVED_GET_BATTERY_CHARGE == byteBuffer[0]) {
                            received_length = inputStream.read(received_params, 0, 3);
                            for (int i = received_length; i < 3; i++) {
                                inputStream.read(byteBuffer);
                                received_params[i] = byteBuffer[0];
                            }

                            return received_params[2];
                        }
                    }
                }
                count++;
            }
        }catch (IOException e){

        }

        return -1;
    }

    public boolean getSensorData(){
        try {
            boolean accGyroOk = false, magOk = false;
            while (true) {
                if(-1 != inputStream.read(byteBuffer)) {
                    if (PROTOCOL_HEADER == byteBuffer[0]){
                        inputStream.read(byteBuffer);
                        if(RECEIVED_ACC_GYRO_DATA == byteBuffer[0]){
                            received_length = inputStream.read(received_params, 0, 23);

                            for(int i=received_length; i<22; i++){
                                inputStream.read(byteBuffer);
                                received_params[i] = byteBuffer[0];
                            }

                            time = get4Byte(received_params, 0);
                            acc_x = get3Byte(received_params, 4);
                            acc_y = get3Byte(received_params, 7);
                            acc_z = get3Byte(received_params, 10);

                            gyr_x = get3Byte(received_params, 13);
                            gyr_y = get3Byte(received_params, 16);
                            gyr_z = get3Byte(received_params, 19);

                            waitCount = 0;
                            accGyroOk = true;
                        } else if (RECEIVED_MAG_DATA == byteBuffer[0]) {

                            received_length = inputStream.read(received_params, 0, 13);

                            for(int i=received_length; i<13; i++){
                                inputStream.read(byteBuffer);
                                received_params[i] = byteBuffer[0];
                            }

//                            time = get4Byte(received_params, 0);
                            mag_x = get3Byte(received_params, 4);
                            mag_y = get3Byte(received_params, 7);
                            mag_z = get3Byte(received_params, 10);
                            waitCount = 0;
                            magOk = true;
                        }

                    }
                }
//                Log.v("OK", "ACC " + accGyroOk + ", mag " + magOk);
                if (accGyroOk && magOk) break;
            }
        }catch (IOException e){
            waitCount++;
            if(waitCount > 3){
                isConnected = false;
                return false;
            }
        }

        return true;
    }

    @Override
    public void cancelSensorThread(){
        if(measuringTimer != null) {
            measuringTimer.cancel();
        }
    }

    @Override
    public int getAccX(){
        return acc_x;
    }

    @Override
    public int getAccY(){
        return acc_y;
    }

    @Override
    public int getAccZ(){
        return acc_z;
    }

    @Override
    public int getGyrX(){
        return gyr_x;
    }

    @Override
    public int getGyrY(){
        return gyr_y;
    }

    @Override
    public int getGyrZ(){
        return gyr_z;
    }

    @Override
    public int getMagX() {
        return mag_x;
    }

    @Override
    public int getMagY() {
        return mag_y;
    }

    @Override
    public int getMagZ() {
        return mag_z;
    }

    int getByteOffset;
    int getByteResult;

    protected int get3Byte(byte[] buffer, int offset){
        getByteOffset = offset;
        getByteResult = 0;

        getByteResult |= buffer[getByteOffset++] & 0x000000FF;
        getByteResult |= (buffer[getByteOffset++] & 0x000000FF) << 8;
        getByteResult |= buffer[getByteOffset++] << 16;

        return getByteResult;
    }

    protected int get4Byte(byte[] buffer, int offset){
        getByteOffset = offset;
        getByteResult = 0;

        getByteResult |= buffer[getByteOffset++] & 0x000000FF;
        getByteResult |= (buffer[getByteOffset++] & 0x000000FF) << 8;
        getByteResult |= (buffer[getByteOffset++] & 0x000000FF) << 16;
        getByteResult |= buffer[getByteOffset++] << 24;

        return getByteResult;
    }

    protected void startMeasuring(){
        //計測開始
        byte[] params = new byte[] {
                (byte) 0,
                (byte) 0, (byte) 1, (byte) 1,
                (byte) 0, (byte) 0, (byte) 0,
                (byte) 0,
                (byte) 0, (byte) 1, (byte) 1,
                (byte) 0, (byte) 0, (byte) 0
        };
        sendCommand(COMMAND_START_MEASURING, params);

        try {
            if (PROTOCOL_HEADER == (byte)inputStream.read()){
                if(RECEIVED_START_MEASURING == (byte)inputStream.read()) {

                }
            }
        }catch (IOException e) {

        }
    }

    protected void stopMeasuring(){
        //計測開始
        byte[] params = new byte[] {
                (byte) 0
        };
        sendCommand(COMMAND_STOP_MEASURING, params);
    }

    public void sendCommand(int command, byte[] params){
        bcc = PROTOCOL_HEADER;
        bcc ^= command;
        for (byte b : params) {
            bcc ^= b;
        }

        if(outputStream == null) return;

        try {
            outputStream.write(PROTOCOL_HEADER);
            outputStream.write(command);
            outputStream.write(params);
            outputStream.write(bcc);
            outputStream.flush();
        }
        catch (IOException e) {

        }
    }

    protected void initSetting() {
        //加速度・ジャイロ基本設定
        byte[] params = new byte[]{
                (byte) sampling_interval_msec,  //インターバル（msec）
                1,  //計測データ送信回数
                0   //内部記録回数
        };
        sendCommand(COMMAND_ACCGYR_SETTING, params);

        //Mag基本設定
        params = new byte[]{
                (byte) sampling_interval_msec,  //インターバル（msec）
                1,  //計測データ送信回数
                0   //内部記録回数
        };
        sendCommand(COMMAND_MAG_SETTING, params);

        //加速度レンジ設定
        params = new byte[]{
                3   //0:±2G,1:±4G,2:±8G,3:±16G
        };
        sendCommand(COMMAND_ACC_RANGE_SETTING, params);

        //ジャイロレンジ設定
        params = new byte[]{
                3   //0:±250dps,1:±500dps,2:±1000dps,3:±2000dp
        };
        sendCommand(COMMAND_GYR_RANGE_SETTING, params);

        //Mag設定
//        params = new byte[]{
//                0   //0
//        };
//        sendCommand(COMMAND_MAG_CALIBRATION_SETTING, params);
    }

    @Override
    public boolean isConnected(){
        return isConnected;
    }

    @Override
    public void clearBuffer(){
        isClearBuffering = true;

        try {
            inputStream.read(buffer);
            inputStream.read(buffer);
            inputStream.read(buffer);
        }catch (IOException e) {

        }

        isClearBuffering = false;
    }

    public boolean isClearBuffering(){
        return isClearBuffering;
    }

    @Override
    public void setDeviceAddress(String address){
        this.deviceAddress = address;
    }
}
