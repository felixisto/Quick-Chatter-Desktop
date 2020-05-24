/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import quickchatter.utilities.Logger;

/**
 * The application text values are here, including the localization values.
 *
 * @author Bytevi
 */
public enum TextValue {
    // Version
    Version,
    
    // Errors
    Error_Generic,
    
    // Screen - Connect Menu
    ScreenConnectMenu_Title,
    
    // Screen - Connect
    ScreenConnect_Title,
    ScreenConnect_ScanON, ScreenConnect_ScanOFF,
    
    // Screen - Reconnect
    ScreenReconnect_Title,
    
    // Screen - Connecting
    ScreenConnecting_Title,
    
    // Screen - Chat
    ScreenChat_Title,
    
    // File Picker
    FilePicker_GenericTitle,
    
    End;
    
    // Get text
    public static final String placeholderSymbol = "@!";
    
    public static String getText(TextValue value)
    {
        String string = getValues().get(value);
        
        return evaluateTextValue(value, string, null, null, null);
    }
    
    public static String getText(TextValue value, String arg1)
    {
        String string = getValues().get(value);
        
        return evaluateTextValue(value, string, arg1, null, null);
    }
    
    public static String getText(TextValue value, String arg1, String arg2)
    {
        String string = getValues().get(value);
        
        return evaluateTextValue(value, string, arg1, arg2, null);
    }
    
    public static String getText(TextValue value, String arg1, String arg2, String arg3)
    {
        String string = getValues().get(value);
        
        return evaluateTextValue(value, string, arg1, arg2, arg3);
    }
    
    public static String evaluateTextValue(TextValue value, String textValueString, String arg1, String arg2, String arg3)
    {
        if (textValueString == null)
        {
            recordMissingValue(value);
            return "";
        }
        
        if (arg1 != null)
        {
            arg1 = preprocessTextArgument(arg1);
            textValueString = textValueString.replaceFirst(placeholderSymbol, arg1);
            
            if (arg2 != null)
            {
                arg2 = preprocessTextArgument(arg2);
                textValueString = textValueString.replaceFirst(placeholderSymbol, arg2);
                
                if (arg3 != null)
                {
                    arg3 = preprocessTextArgument(arg3);
                    textValueString = textValueString.replaceFirst(placeholderSymbol, arg3);
                }
            }
        }
        
        return textValueString;
    }
    
    private static String preprocessTextArgument(String arg)
    {
        // Backslashes and dollar signs will not work when replacing a target string with the String.replaceX methods.
        // Use Matcher to get a proper string literal
        return Matcher.quoteReplacement(arg);
    }
    
    private static void recordMissingValue(TextValue value)
    {
        String key = value.name();
        
        boolean alreadyRecorded = !missingValues.add(key);
        
        if (!alreadyRecorded)
        {
            Logger.warning(TextValue.class.getCanonicalName(), "Could not find string for text value " + key + "!");
        }  
    }
    
    // Values
    private static HashMap<TextValue, String> getValues()
    {
        // You can return a different map here, depending on the app currently selected language
        return englishValues;
    }
    
    // Any invalid request values will be added to this set.
    // This is done to prevent warning log spam.
    private static final HashSet<String> missingValues = new HashSet<>();
    
    private static final HashMap<TextValue, String> englishValues = new HashMap<TextValue, String>() {{
        put(Version, "0.1");
        
        // Errors
        put(Error_Generic, "Error");
        
        // Screen - Connect Menu
        put(ScreenConnectMenu_Title, "Quick Chatter");
        
        // Screen - Connect
        put(ScreenConnect_Title, "Quick Chatter");
        put(ScreenConnect_ScanON, "Stop");
        put(ScreenConnect_ScanOFF, "Scan");
        
        // Screen - Reconnect
        put(ScreenReconnect_Title, "Quick Chatter");
        
        // Screen - Connecting
        put(ScreenConnecting_Title, "Quick Chatter");
        
        // Screen - Chat
        put(ScreenChat_Title, "Quick Chatter");
        
        // File Picker
        put(FilePicker_GenericTitle, "File Picker");
    }};
}

