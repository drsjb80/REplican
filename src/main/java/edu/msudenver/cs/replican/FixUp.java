package edu.msudenver.cs.replican;

// THREADSAFE_LEVEL_GREY
// not sure what this class is for...
class FixUp
{
    public static void main (String args[])
    {
        System.out.println ("Rose'sC".replaceAll ("\'", "\\\\'"));
    }
}
