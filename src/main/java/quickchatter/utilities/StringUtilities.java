/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.utilities;

import java.util.Locale;

public class StringUtilities {
    public static String secondsToString(double durationInSeconds)
    {
        final int time = (int)durationInSeconds;
        final int hr = time/60/60;
        final int min = (time - (hr*60*60)) / 60;
        final int sec = (time - (hr*60*60) - (min*60));

        if (hr == 0)
        {
            if (min < 10)
            {
                final String strMin = Integer.toString(min);
                final String strSec = parseToStringWithLeadingZero(sec);

                return String.format("%s:%s", strMin, strSec);
            }

            final String strMin = parseToStringWithLeadingZero(min);
            final String strSec = parseToStringWithLeadingZero(sec);

            return String.format("%s:%s", strMin, strSec);
        }

        final String strHr = parseToStringWithLeadingZero(hr);
        final String strMin = parseToStringWithLeadingZero(min);
        final String strSec = parseToStringWithLeadingZero(sec);

        return String.format("%s:%s:%s", strHr, strMin, strSec);
    }

    public static String parseToStringWithLeadingZero(int number)
    {
        return String.format(Locale.getDefault(), "%02d", number);
    }

    public static int occurrencesCount(String string, String substring) {
        return string.length() - string.replace(substring, "").length();
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length());
        } else {
            return string;
        }
    }
}

