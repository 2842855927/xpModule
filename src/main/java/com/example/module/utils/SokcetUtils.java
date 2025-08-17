package com.example.module.utils;

import androidx.annotation.NonNull;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Zhenxi on 2020-07-29
 */
public  class SokcetUtils {





    public interface OnRecvDataOK {
         void Message(String message, SocketClient client);
    }


    private static Method TestMethod;
    private static OnRecvDataOK recvDataOK;

    //public static String IP ="47.92.138.16";

    //自己服务器
    public static String IP ="122.51.51.230";

    public static String Tags ="[\"XCList\",\"XCDetailed\",\"XCRoomList\"]";



    public static void init(Method method,OnRecvDataOK TcpRecvDataOK){
        if (TestMethod == null) {
            TestMethod=method;
            recvDataOK= TcpRecvDataOK;
            method.setAccessible(true);
            initTenCompanySocketNet();
        }
    }

    private static void initTenCompanySocketNet() {
        for (int i = 0; i < 5; i++) {

            SocketClient client = new SocketClient();
            // 于健 - 106.52.40.24
            client.getAddress().setRemoteIP(IP).setRemotePort(8089);

            client.getAddress().setConnectionTimeout(15 * 1000); // 连接超时时长，单位毫秒

            client.setCharsetName("utf-8");

            client.registerSocketClientDelegate(new MySocketClientDelegate());

            client.connect(); // 连接，异步进行
        }
        CLogUtils.e("初始化5个客户端完毕");
    }

    private  static  class MySocketClientDelegate implements SocketClientDelegate{
        private byte[] recvData = null;

        @Override
        public void onConnected(SocketClient client) {
            CLogUtils.e("gamesocket连接成功  ");
            recvData = null;
            socketSendRegistMessage(Tags, client);
        }

        @Override
        public void onDisconnected(SocketClient client) {
            CLogUtils.e("gamesocket连接断开");
            try {
                Thread.sleep(1000);
                //MainActivity.initCompanySocketNet();
                iniTheSokcetClient();
            } catch (Exception e) {

            }
        }

        @Override
        public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
            // String message = responsePacket.getMessage(); // 获取按默认设置的编码转化的String，可能为null
            // new packet
            if (recvData == null) { // kong
                recvData = responsePacket.getData();
            } else {                // last half package
                recvData = byteMerger(recvData, responsePacket.getData());
            }

            do {
                if (recvData != null) {
                    if (recvData.length < 4) {
                        //CLogUtils.e("recvData.length 半包, length < 4");
                        return; // 半包
                    }

                    int len = ByteBuffer.wrap(recvData, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt();
                    if (recvData.length - 4 < len) { // 半包
                        //CLogUtils.e("length 半包, length < 4");
                        return;
                    }

                    byte[] pack = getBody(recvData, len);

                    recvDataOK.Message(new String(pack),client);

                    if (recvData.length == len + 4) {
                        recvData = null;
                    } else { // recvData.length > len + 4
                        byte[] newB = new byte[recvData.length - len + 4];
                        System.arraycopy(recvData, len + 4, newB, 0, recvData.length - len + 4);
                        recvData = newB;
                    }
                }
            } while (recvData != null);
        }
        /**
         * 当前 进程的 client
         * 如果 某个 进程断开了  重新初始化一个 新的
         */
        private void iniTheSokcetClient() {
            //for (int i = 0; i < 5; i++) {

                SocketClient client = new SocketClient();
                // 于健 - 106.52.40.24
                // 公司 - "47.104.25.224"
                client.getAddress().setRemoteIP(IP).setRemotePort(8089);

                client.getAddress().setConnectionTimeout(15 * 1000); // 连接超时时长，单位毫秒

                client.setCharsetName("utf-8");

                client.registerSocketClientDelegate(new MySocketClientDelegate());

                client.connect(); // 连接，异步进行
            }
        //}
    }


    private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    private static byte[] getBody(byte[] msg, int len) {
        byte[] pack = new byte[len];
        System.arraycopy(msg, 4, pack, 0, len);
        return pack;
    }

    //发送消息
    public static void socketSendRegistMessage(String info, SocketClient socketClient) {
        CLogUtils.e("发送消息内容 是   " + info);
        String status = String.valueOf(socketClient.getState());
        if (socketClient != null && status.equals("Connected")) {
            byte[] bys = info.getBytes();
            byte[] sendByts = new byte[bys.length + 4];
            ByteBuffer.wrap(sendByts, 0, 4).order(ByteOrder.BIG_ENDIAN).putInt(bys.length);
            System.arraycopy(bys, 0, sendByts, 4, bys.length);
            socketClient.sendData(sendByts); // 发送byte[]消息
        }
    }


    //发送消息
    public static void socketSendMessage(String info, SocketClient socketClient) {
        //CLogUtils.e("发送消息内容 是   " + info);
        String status = String.valueOf(socketClient.getState());
        if (socketClient != null && status.equals("Connected")) {
            byte[] bys = info.getBytes();
            byte[] sendByts = new byte[bys.length + 4];
            ByteBuffer.wrap(sendByts, 0, 4).order(ByteOrder.BIG_ENDIAN).putInt(bys.length);
            System.arraycopy(bys, 0, sendByts, 4, bys.length);
            socketClient.sendData(sendByts); // 发送byte[]消息
        }
    }


}
