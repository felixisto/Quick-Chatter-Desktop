/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.utilities;

import org.jetbrains.annotations.NotNull;

public interface Parser <Source, Destination> {
    @NotNull Destination parse(@NotNull Source data) throws Exception;
}
