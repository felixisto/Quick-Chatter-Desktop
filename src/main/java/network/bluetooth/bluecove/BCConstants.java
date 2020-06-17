/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.bluecove;

import org.jetbrains.annotations.NotNull;
import network.basic.TransmissionType;
import utilities.TimeValue;

public class BCConstants {
    private static BCConstants shared;

    public final TransmissionType TYPE_PING;
    public final TransmissionType TYPE_CHAT; // value: text
    public final TransmissionType TYPE_SEND_FILE_ASK; // value: file description
    public final TransmissionType TYPE_SEND_FILE_STATUS; // value: 0 cancel 1 accept 2 deny
    public final TransmissionType TYPE_SEND_FILE_DATA; // value: file data
    public final TransmissionType TYPE_SEND_FILE_FINAL_CONFIRM;

    public static final @NotNull TimeValue DEFAULT_PING_DELAY = TimeValue.buildSeconds(1);
    public static final @NotNull TimeValue CONNECTION_TIMEOUT = TimeValue.buildSeconds(45);
    public static final @NotNull TimeValue CONNECTION_TIMEOUT_WARNING = TimeValue.buildSeconds(15);

    private BCConstants() {
        TransmissionType ping;
        TransmissionType chatType;
        TransmissionType sendFileAsk;
        TransmissionType sendFileStatus;
        TransmissionType sendFileData;
        TransmissionType sendFileConfirm;

        try {
            ping = new TransmissionType("Ping");
            chatType = new TransmissionType("Chat");
            sendFileAsk = new TransmissionType("FAsk");
            sendFileStatus = new TransmissionType("FSta");
            sendFileData = new TransmissionType("FDat");
            sendFileConfirm = new TransmissionType("FEnd");
        } catch (Exception e) {
            ping = null;
            chatType = null;
            sendFileAsk = null;
            sendFileStatus = null;
            sendFileData = null;
            sendFileConfirm = null;
        }

        this.TYPE_PING = ping;
        this.TYPE_CHAT = chatType;
        this.TYPE_SEND_FILE_ASK = sendFileAsk;
        this.TYPE_SEND_FILE_STATUS = sendFileStatus;
        this.TYPE_SEND_FILE_DATA = sendFileData;
        this.TYPE_SEND_FILE_FINAL_CONFIRM = sendFileConfirm;
    }

    public static synchronized @NotNull BCConstants getShared() {
        if (shared == null) {
            shared = new BCConstants();
        }

        return shared;
    }
}

