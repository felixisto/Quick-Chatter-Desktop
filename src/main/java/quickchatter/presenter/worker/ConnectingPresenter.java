/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter.worker;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.basic.TransmissionReadStream;
import quickchatter.network.basic.TransmissionWriteStream;
import quickchatter.network.bluetooth.basic.BESocket;
import quickchatter.network.bluetooth.bluecove.BCConstants;
import quickchatter.network.bluetooth.bluecove.BCSocket;
import quickchatter.network.bluetooth.bluecove.other.BCStandardReadWriteBandwidth;
import quickchatter.network.bluetooth.bluecove.other.BCStandardReadWriteBandwidthMonitor;
import quickchatter.network.bluetooth.bluecove.transmission.BCTransmissionLine;
import quickchatter.network.bluetooth.bluecove.transmission.BCTransmissionLineBuilder;
import quickchatter.network.bluetooth.bluecove.transmission.BCTransmitter;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Errors;

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

