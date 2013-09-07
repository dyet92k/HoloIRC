/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.uiircinterface;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.irc.Channel;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.PrivateMessageUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.User;
import com.fusionx.lightirc.irc.event.ChannelEvent;
import com.fusionx.lightirc.irc.event.ConnectedEvent;
import com.fusionx.lightirc.irc.event.FinalDisconnectEvent;
import com.fusionx.lightirc.irc.event.JoinEvent;
import com.fusionx.lightirc.irc.event.MentionEvent;
import com.fusionx.lightirc.irc.event.NickInUseEvent;
import com.fusionx.lightirc.irc.event.PartEvent;
import com.fusionx.lightirc.irc.event.PrivateMessageEvent;
import com.fusionx.lightirc.irc.event.RetryPendingDisconnectEvent;
import com.fusionx.lightirc.irc.event.ServerEvent;
import com.fusionx.lightirc.irc.event.SwitchToServerEvent;
import com.fusionx.lightirc.irc.event.UserEvent;
import com.fusionx.lightirc.irc.event.UserListReceivedEvent;
import com.fusionx.lightirc.ui.IRCActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.HashMap;

public class MessageSender {
    private static final HashMap<String, MessageSender> mSenderMap = new HashMap<>();

    private Context mContext;
    private boolean mDisplayed;
    private IRCBus mBus;
    private String mServerName;

    private MessageSender() {
    }

    public void initialSetup(Context context) {
        mContext = context;
    }

    public static MessageSender getSender(final String serverName, final boolean nullable) {
        synchronized (mSenderMap) {
            MessageSender sender = mSenderMap.get(serverName);
            if (sender == null && !nullable) {
                sender = new MessageSender();
                sender.mServerName = serverName;
                mSenderMap.put(serverName, sender);
            }
            return sender;
        }
    }

    public static MessageSender getSender(final String serverName) {
        return getSender(serverName, false);
    }

    public static void clear() {
        synchronized (mSenderMap) {
            mSenderMap.clear();
        }
    }

    public static void removeSender(final String serverName) {
        synchronized (mSenderMap) {
            mSenderMap.remove(serverName);
        }
    }

    public Bus getBus() {
        if (mBus == null) {
            mBus = new IRCBus(ThreadEnforcer.ANY);
        }
        return mBus;
    }

    public void setDisplayed(final boolean toast) {
        mDisplayed = toast;
    }

    /**
     * Start of sending messages
     */
    private void sendServerEvent(final Server server, final ServerEvent event) {
        //if(mDisplayed) {
        // Send message to the fragment if it exists
        mBus.post(server, event);
        //} else {
        // Append to buffer in the channel
        //    server.onServerEvent(event);
        //}
    }

    private void sendChannelEvent(final Server server, final Channel channel,
                                  final ChannelEvent event) {
        //if(mDisplayed) {
        // Send message to the fragment if it exists
        mBus.post(server, channel, event);
        //} else {
        // Append to buffer in the channel
        //    channel.onChannelEvent(event);
        //}
    }

    private void sendUserEvent(final Server server, final PrivateMessageUser user,
                               final UserEvent event) {
        //if(mDisplayed) {
        // Send message to the fragment if it exists
        mBus.post(server, user, event);
        //} else {
        // Append to buffer in the channel
        //    user.onUserEvent(event);
        //}
    }

    /*
    End of internal methods
     */

    // Generic events start
    public void sendGenericServerEvent(final Server server, final String message) {
        final ServerEvent event = new ServerEvent(message);
        sendServerEvent(server, event);
    }

    public void sendGenericChannelEvent(final Server server, final Channel channel,
                                        final String message, final boolean userListChanged) {
        final ChannelEvent event = new ChannelEvent(channel.getName(), message,
                userListChanged);
        sendChannelEvent(server, channel, event);
    }

    private void sendGenericUserEvent(final Server server, final PrivateMessageUser user,
                                      final String message) {
        final UserEvent privateMessageEvent = new UserEvent(user.getNick(), message);
        sendUserEvent(server, user, privateMessageEvent);
    }
    // Generic events end

    public void sendFinalDisconnection(final Server server, final String disconnectLine,
                                       final boolean expectedDisconnect) {
        final FinalDisconnectEvent event = new FinalDisconnectEvent(expectedDisconnect,
                disconnectLine);
        // Send message to the fragment if it exists
        mBus.post(server, event);
    }

    public void sendRetryPendingServerDisconnection(final Server server,
                                                    final String disconnectLine) {
        final RetryPendingDisconnectEvent event = new RetryPendingDisconnectEvent(disconnectLine);
        // Send message to the fragment if it exists
        mBus.post(server, event);
    }

    public void sendNewPrivateMessage(final String nick) {
        mBus.post(new PrivateMessageEvent(nick));
    }

    public void sendChanelJoined(final String channelName) {
        mBus.post(new JoinEvent(channelName));
    }

    public void sendChanelParted(final String channelName) {
        mBus.post(new PartEvent(channelName));
    }

    public void sendPrivateAction(final Server server, final PrivateMessageUser user,
                                  final User sendingUser,
                                  final String rawAction) {
        String message = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getColorfulNick(), rawAction);
        // TODO - change this to be specific for PMs
        if (sendingUser.equals(user)) {
            mention(user.getNick());
        }
        sendGenericUserEvent(server, user, message);
    }

    public void sendChannelAction(final Server server, final String userNick, final Channel channel,
                                  final ChannelUser sendingUser, final String rawAction) {
        String finalMessage = String.format(mContext.getString(R.string.parser_action),
                sendingUser.getPrettyNick(channel), rawAction);
        if (rawAction.toLowerCase().contains(userNick.toLowerCase())) {
            mention(channel.getName());
            finalMessage = "<b>" + finalMessage + "</b>";
        }
        sendGenericChannelEvent(server, channel, finalMessage, false);
    }

    /**
     * Method used to send a private message.
     * <p/>
     * Method should not be used from anywhere but the Server class.
     *
     * @param user       - the destination user object
     * @param sending    - the user who is sending the message - it may be us or it may
     *                   be the other user
     * @param rawMessage - the message being sent
     */
    public void sendPrivateMessage(final Server server, final PrivateMessageUser user,
                                   final User sending, final String rawMessage) {
        final String message = String.format(mContext.getString(R.string.parser_message),
                sending.getColorfulNick(), rawMessage);
        // TODO - change this to be specific for PMs
        mention(user.getNick());
        sendGenericUserEvent(server, user, message);
    }

    public void sendMessageToChannel(final Server server, final String userNick,
                                     final Channel channel, final String sendingNick,
                                     final String rawMessage) {
        String preMessage = String.format(mContext.getString(R.string.parser_message),
                sendingNick, rawMessage);
        if (rawMessage.toLowerCase().contains(userNick.toLowerCase())) {
            mention(channel.getName());
            preMessage = "<b>" + preMessage + "</b>";
        }
        sendGenericChannelEvent(server, channel, preMessage, false);
    }

    public void sendNickInUseMessage(final Server server) {
        final NickInUseEvent event = new NickInUseEvent(mContext);
        sendServerEvent(server, event);
    }

    public void switchToServerMessage(final Server server, final String message) {
        final SwitchToServerEvent event = new SwitchToServerEvent(message);
        sendServerEvent(server, event);
    }

    public void sendUserListReceived(final Channel channel) {
        //if (mDisplayed) {
        mBus.post(new UserListReceivedEvent(channel.getName()));
        //}
    }

    public void sendConnected(final Server server, final String url) {
        final ConnectedEvent event = new ConnectedEvent(mContext, url, server.getTitle());
        sendServerEvent(server, event);
    }

    public void mention(final String messageDestination) {
        if (mDisplayed) {
            mBus.post(new MentionEvent(messageDestination));
        } else {
            final NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setContentTitle(mContext.getString(R.string.app_name))
                    .setContentText(mContext.getString(R.string.service_you_mentioned) + " " +
                            messageDestination)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true)
                    .setTicker(mContext.getString(R.string.service_you_mentioned) + " " +
                            messageDestination);
            final Intent mIntent = new Intent(mContext, IRCActivity.class);
            mIntent.putExtra("serverTitle", mServerName);
            mIntent.putExtra("mention", messageDestination);
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
            taskStackBuilder.addParentStack(IRCActivity.class);
            taskStackBuilder.addNextIntent(mIntent);
            final PendingIntent pIntent = taskStackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationManager.notify(345, builder.setContentIntent(pIntent).build());
        }
    }
}