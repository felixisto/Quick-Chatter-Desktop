/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.utilities;

import java.net.URI;
import org.jetbrains.annotations.NotNull;

public interface Path {
    @NotNull URI getURL();
    @NotNull String getPath();
    @NotNull String getLastComponent();
    @NotNull String getSeparator();
}
