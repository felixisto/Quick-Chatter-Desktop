/*
 * Created by Kristiyan Butev.
 * Copyright © 2019 Kristiyan Butev. All rights reserved.
 */
package utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Defines commonly used display alert dialog methods.
 * 
 * @author Bytevi
 */
public class AlertWindows {
    public static void showMessage(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message, 
            @NotNull String buttonText)
    {
        showMessage(parent, title, message, buttonText, null);
    }
    
    public static void showMessage(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message, 
            @NotNull String buttonText,
            @Nullable SimpleCallback okCallback)
    {
        Object[] options = {buttonText};
        
        int result = JOptionPane.showOptionDialog(parent,
                message, title,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                null, 
                options,
                options[0]);
        
        if (okCallback != null)
        {
            if (result == JOptionPane.OK_OPTION)
            {
                okCallback.perform();
            }
        }
    }
    
    public static void showConfirmationMessage(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message,
            @Nullable SimpleCallback confirmCallback,
            @Nullable SimpleCallback cancelCallback)
    {
        int result = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION)
        {
            if (confirmCallback != null)
            {
                confirmCallback.perform();
            }
        }
        else if (result == JOptionPane.NO_OPTION)
        {
            if (cancelCallback != null)
            {
                cancelCallback.perform();
            }
        }
    }
    
    // If the dialog is closed, no callback is performed.
    public static void showChoice(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message, 
            @NotNull String choiceAText,
            @NotNull String choiceBText,
            @Nullable SimpleCallback aCallback,
            @Nullable SimpleCallback bCallback)
    {
        showChoice(parent, title, message, choiceAText, choiceBText, aCallback, bCallback, null);
    }
    
    public static void showChoice(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message, 
            @NotNull String choiceAText,
            @NotNull String choiceBText,
            @Nullable SimpleCallback aCallback,
            @Nullable SimpleCallback bCallback,
            @Nullable SimpleCallback cancelCallback)
    {
        Object[] options = {choiceAText, choiceBText};
        
        int result = JOptionPane.showOptionDialog(parent,
                message, title,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                null, 
                options,
                options[0]);
        
        if (result == 0 && aCallback != null)
        {
            aCallback.perform();
            return;
        }
        
        if (result == 1 && bCallback != null)
        {
            bCallback.perform();
            return;
        }
        
        if (cancelCallback != null) {
            cancelCallback.perform();
        }
    }
    
    public static void showErrorMessage(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message, 
            @NotNull String buttonText)
    {
        showErrorMessage(parent, title, message, buttonText, null);
    }
    
    public static void showErrorMessage(@NotNull Component parent, 
            @NotNull String title,
            @NotNull String message, 
            @NotNull String buttonText,
            @Nullable SimpleCallback okCallback)
    {
        Object[] options = {buttonText};
        
        int result = JOptionPane.showOptionDialog(parent,
                message, title,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE,
                null, 
                options,
                options[0]);
        
        if (okCallback != null)
        {
            if (result == JOptionPane.OK_OPTION)
            {
                okCallback.perform();
            }
        }
    }
    
    public static void showTextField(@NotNull Component parent, 
            @NotNull String message, 
            @NotNull String initialString,
            @NotNull Callback<String> callback)
    {
        Object result = JOptionPane.showInputDialog(parent, message, initialString);
        
        if (result == null) {
            callback.perform(null);
        } else {
            callback.perform((String)result);
        }
    }
}
