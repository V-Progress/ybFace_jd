package com.yunbiao.ybsmartcheckin_live_id.xmpp;

import android.content.Intent;
import android.util.Log;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

public class NotificationPacketListener implements PacketListener {

    private static final String LOGTAG = LogUtil.makeLogTag(NotificationPacketListener.class);

    private final XmppManager xmppManager;

    public NotificationPacketListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    @Override
    public void processPacket(Packet packet) {
        Log.d(LOGTAG, "NotificationPacketListener.processPacket()...");
        Log.d(LOGTAG, "packet.toXML()=" + packet.toXML());

        if (packet instanceof NotificationIQ) {
            NotificationIQ notification = (NotificationIQ) packet;

            if (notification.getChildElementXML().contains("androidpn:iq:notification")) {
                String notificationId = notification.getId();
                String notificationApiKey = notification.getApiKey();
                String notificationTitle = notification.getTitle();
                String notificationMessage = notification.getMessage();
                String notificationUri = notification.getUri();
                String notificationFrom = notification.getFrom();
                String packetId = notification.getPacketID();
                Intent intent = new Intent(Constants.ACTION_SHOW_NOTIFICATION);
                if (notificationTitle.contains("meet")) {
                    intent = new Intent(Constants.MEETING);
                } else if (notificationTitle.contains("queue")) {
                    intent = new Intent(Constants.QUEUE);
                } else if (notificationTitle.contains("print")) {
                    intent = new Intent(Constants.PRINT);
                } else if (notificationTitle.contains("weixin")) {
                    intent = new Intent(Constants.WEIXIN);
                }
                intent.putExtra(Constants.NOTIFICATION_ID, notificationId);
                intent.putExtra(Constants.NOTIFICATION_API_KEY, notificationApiKey);
                intent.putExtra(Constants.NOTIFICATION_TITLE, notificationTitle);
                intent.putExtra(Constants.NOTIFICATION_MESSAGE, notificationMessage);
                intent.putExtra(Constants.NOTIFICATION_URI, notificationUri);
                intent.putExtra(Constants.NOTIFICATION_FROM, notificationFrom);
                intent.putExtra(Constants.PACKET_ID, packetId);

                //发送收到通知回执
                IQ result = NotificationIQ.createResultIQ(notification);

                try {
                    xmppManager.getConnection().sendPacket(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                xmppManager.getContext().sendBroadcast(intent);
            }
        }
    }
}
