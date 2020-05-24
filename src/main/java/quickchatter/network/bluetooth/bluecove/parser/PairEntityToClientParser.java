/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.parser;

import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BEPairing;
import quickchatter.utilities.Parser;

public class PairEntityToClientParser implements Parser<BEPairing.Entity, BEClient> {
    @Override
    public @NotNull BEClient parse(@NotNull BEPairing.Entity data) throws Exception {
        return data.getClient();
    }
}
