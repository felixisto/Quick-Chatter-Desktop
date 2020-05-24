/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui;

import org.jetbrains.annotations.NotNull;
import quickchatter.navigation.Router;
import quickchatter.ui.view.BaseView;
import quickchatter.ui.view.ChatFrame;
import quickchatter.ui.view.ConnectFrame;
import quickchatter.ui.view.ConnectMenuFrame;
import quickchatter.ui.view.ConnectingFrame;
import quickchatter.ui.view.ReconnectFrame;

public class ViewBuilder {
    @NotNull Router.Primary _router;
    
    public ViewBuilder(@NotNull Router.Primary router) {
        _router = router;
    }
    
    public @NotNull ConnectMenuFrame buildConnectMenuScreen() {
        ConnectMenuFrame view = new ConnectMenuFrame();
        return view;
    }

    public @NotNull ConnectFrame buildConnectScreen() {
        ConnectFrame view = new ConnectFrame();
        return view;
    }

    public @NotNull ReconnectFrame buildReconnectScreen() {
        ReconnectFrame view = new ReconnectFrame();
        return view;
    }

    public @NotNull ConnectingFrame buildConnectingScreen() {
        ConnectingFrame view = new ConnectingFrame();
        return view;
    }

    public @NotNull ChatFrame buildChatScreen() {
        ChatFrame view = new ChatFrame();
        return view;
    }

    public @NotNull BaseView.Whole buildPickFileScreen() {
        return null;
    }

    public @NotNull BaseView.Whole buildPickFileDestinationScreen() {
        return null;
    }
}
