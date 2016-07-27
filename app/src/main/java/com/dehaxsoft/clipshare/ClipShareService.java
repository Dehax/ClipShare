package com.dehaxsoft.clipshare;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClipShareService extends IntentService {

    public static final int SERVER_PORT = 49001;
    private static final int MESSAGE_BUFFER = 8192;

    private byte[] mMessage = new byte[MESSAGE_BUFFER];

    public ClipShareService() {
        super("ClipShareService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatagramPacket mDatagramPacket = new DatagramPacket(mMessage, MESSAGE_BUFFER);

        try {
            DatagramSocket mDatagramSocket = new DatagramSocket(SERVER_PORT);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            while (true) {
                mDatagramSocket.receive(mDatagramPacket);

                byte code = mMessage[0];

                if (code == -1) {
                    break;
                } else if (code == 0) {
                    DatagramPacket response = new DatagramPacket(new byte[] { 1 }, 1);
                    response.setSocketAddress(mDatagramPacket.getSocketAddress());
                    mDatagramSocket.send(response);
                } else if (code == 1) {
                    int textSize = mMessage[1] * 255 + mMessage[2];
                    String text = new String(mMessage, 3, textSize);

                    ClipData clip = ClipData.newPlainText("Dehax message", text);
                    clipboard.setPrimaryClip(clip);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                    mBuilder.setSmallIcon(R.drawable.ic_assignment_black_24dp);
                    mBuilder.setContentTitle(getString(R.string.app_name));
                    mBuilder.setContentText(getString(R.string.notification_text_copied));
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }

            mDatagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
