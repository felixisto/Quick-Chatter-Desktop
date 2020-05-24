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
import quickchatter.network.bluetooth.bluecove.BDConstants;
import quickchatter.network.bluetooth.bluecove.BDSocket;
import quickchatter.network.bluetooth.bluecove.other.BDStandardReadWriteBandwidth;
import quickchatter.network.bluetooth.bluecove.other.BDStandardReadWriteBandwidthMonitor;
import quickchatter.network.bluetooth.bluecove.transmission.BDTransmissionLine;
import quickchatter.network.bluetooth.bluecove.transmission.BDTransmissionLineBuilder;
import quickchatter.network.bluetooth.bluecove.transmission.BDTransmitter;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Errors;

public class ConnectingPresenter {
    public static @NotNull BDTransmitter startServer(@NotNull BESocket socket) throws Exception {
        BDStandardReadWriteBandwidth readBandwidth = new BDStandardReadWriteBandwidthMonitor(DataSize.buildBytes(1024 * 3));
        BDStandardReadWriteBandwidth writeBandwidth = new BDStandardReadWriteBandwidthMonitor(DataSize.buildBytes(1024 * 3));

        BDSocket bdSocket = null;

        if (socket instanceof BDSocket) {
            bdSocket = (BDSocket) socket;
        } else {
            Errors.throwInvalidArgument("Given socket must be BDSocket.");
        }

        TransmissionReadStream input = new TransmissionReadStream(bdSocket.getSocket().openInputStream(), readBandwidth);
        TransmissionWriteStream output = new TransmissionWriteStream(bdSocket.getSocket().openOutputStream(), writeBandwidth);

        BDTransmissionLineBuilder builder = new BDTransmissionLineBuilder(input, output);

        List<BDTransmissionLine> lines = new ArrayList<>();
        lines.add(builder.build(BDConstants.getShared().TYPE_CHAT));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_DATA));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_STATUS));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_ASK));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_FINAL_CONFIRM));

        return new BDTransmitter(bdSocket, input, output, lines);
    }
}

