package edu.msudenver.cs.replican;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

public class DelimitedBufferedInputStream extends BufferedInputStream
{
	private static final Logger logger = REplican.logger;
	private List<String> strings = new ArrayList<>();
	private boolean inString;
	private boolean inQuotes;
	private String current;
	private final char start;
	private final char end;
	private int count;

	public DelimitedBufferedInputStream (InputStream in)
	{
		super (in);
		start = end = '"';
	}

	public DelimitedBufferedInputStream (InputStream in, char delimiter)
	{
		super (in);
		start = end = delimiter;
	}

	public DelimitedBufferedInputStream (InputStream in, char start, char end)
	{
		super (in);
		this.start = start;
		this.end  = end;
	}

	public int read (byte[] b) throws IOException
	{
		return (read (b, 0, b.length));
	}

	public int read (byte[] b, int off, int len) throws IOException
	{
		for (int i = off; i <= len; i++)
		{
			int c = read();

			if (c == -1) return (-1);

			b[i] = (byte) c;
		}

		return (len - off);
	}

	public int read() throws IOException
	{
		int c = in.read();
		logger.trace ((char) c);

		if (count++ > 10000)
		{
			logger.debug ("String too long without ending delimiter");
			count = 0;
			inString = false;
			current = "";
		}

		if (inString && !inQuotes && c == '"')
		{
			inQuotes = true;
			current += (char) c;
			return (c);
		}
		else if (inString && inQuotes && c == '"')
		{
			inQuotes = false;
			current += (char) c;
			return (c);
		}
		else if (inString && inQuotes)
		{
			current += (char) c;
			return (c);
		}
		else if (!inString && c == start)
		{
			logger.trace ("start");
			inString = true;
			current = "" + (char) c;
			count = 0;
		}
		else if (inString && c == end)
		{
			inString = false;
			current += (char) c;
			strings.add (current);
			logger.trace ("adding: " + current);
		}
		else if (inString)
		{
			if (c == start)
			{
				logger.debug ("Unexpected delimiter: " + (char) c +
						", ignoring: " + current);
				current = "";
				count = 0;
			}

			current += (char) c;
		}

		return (c);
	}

	public List<String> getStrings()
	{
		// String c[] = new String[strings.size()];
		// strings.toArray (c);
		return (strings);
	}

	public static void main (String args[])
			throws IOException
	{
		DelimitedBufferedInputStream tfis = new DelimitedBufferedInputStream
				(new FileInputStream (args[0]), '<', '>');

		int c;
		while ((c = tfis.read()) != -1)
		{
			System.out.print ((char) c);
		}

		List<String> strings = tfis.getStrings();
		for (String s: strings)
		{
			System.out.println (s);
		}

		tfis.close();
	}
}
