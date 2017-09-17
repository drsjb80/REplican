package edu.msudenver.replican;

import java.lang.reflect.Method;

public class P38
{
 
    	public static Object call (String methodName, Object o, Object args[]) throws Exception 
    	{
    		Class arguments[] = new Class[args.length];
    		
    		for (int i = 0; i < args.length; i++)
    		{
    			arguments[i] = args[i].getClass();
    		}
    		
    		Method m=o.getClass().getDeclaredMethod(methodName, arguments);	
    		m.setAccessible(true);
    		return m.invoke(o, args);
    	}
}
