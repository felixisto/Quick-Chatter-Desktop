/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter.worker;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import network.basic.TransmissionReadStream;
import network.basic.TransmissionWriteStream;
import network.bluetooth.basic.BESocket;
import network.bluetooth.bluecove.BCConstants;
import network.bluetooth.bluecove.BCSocket;
import network.bluetooth.bluecove.other.BCStandardReadWriteBandwidth;
import network.bluetooth.bluecove.other.BCStandardReadWriteBandwidthMonitor;
import network.bluetooth.bluecove.transmission.BCTransmissionLine;
import network.bluetooth.bluecove.transmission.BCTransmissionLineBuilder;
import network.bluetooth.bluecove.transmission.BCTransmitter;
import utilities.DataSize;
import utilities.Errors;

public class ConnectingPresenter {
    public static @NotNull BCTransmitter startServer(@NotNull BESocket socket) throws Exception {
        BCStandardReadWriteBandwidth readBandwidth = new BCStandardReadWriteBandwidthMonitor(DataSize.buildBytes(1024 * 3));
        BCStandardReadWriteBandwidth writeBandwidth = new BCStandardReadWriteBandwidthMonitor(DataSize.buildBytes(1024 * 3));

        BCSocket bdSocket = null;

        if (socket instanceof BCSocket) {
            bdSocket = (BCSocket) socket;
        } else {
            Errors.throwInvalidArgument("Given socket must be BDSocket.");
        }

        TransmissionReadStream input = new TransmissionReadStream(bdSocket.getSocket().openInputStream(), readBandwidth);
        TransmissionWriteStream output = new TransmissionWriteStream(bdSocket.getSocket().openOutputStream(), writeBandwidth);

        BCTransmissionLineBuilder builder = new BCTransmissionLineBuilder(input, output);

        List<BCTransmissionLine> lines = new ArrayList<>();
        lines.add(builder.build(BCConstants.getShared().TYPE_CHAT));
        lines.add(builder.build(BCConstants.getShared().TYPE_SEND_FILE_DATA));
        lines.add(builder.build(BCConstants.getShared().TYPE_SEND_FILE_STATUS));
        lines.add(builder.build(BCConstants.getShared().TYPE_SEND_FILE_ASK));
        lines.add(builder.build(BCConstants.getShared().TYPE_SEND_FILE_FINAL_CONFIRM));

        return new BCTransmitter(bdSocket, input, output, lines);
    }
}

