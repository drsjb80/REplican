package edu.msudenver.replican;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Match
{
    public static void main (String args[])
    {
        Matcher matcher = Pattern.compile (args[0]).matcher (args[1]);

        if (matcher.find())
        {
            System.out.println ("Matched from " + matcher.start() +
                " to " + matcher.end());

            System.out.println (matcher.groupCount());

            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                System.out.println (matcher.group (i));
            }
        }
        else
        {
            System.out.println ("No matches");
        }
    }
}
