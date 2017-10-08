package edu.msudenver.cs.replican;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Test
{
    public static void main (String args[])
    {
        Matcher matcher =
            Pattern.compile ("^\\d{4}-?\\d{5}R?$").matcher ("123456789R");

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
