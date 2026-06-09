package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class REplicanTest {

	private final REplican replican = new REplican();

	@Test
	public void escapeURLNoSpecialChars() throws Throwable {
		assertEquals("foo", P38.call("escapeURL", replican, new Object[]{"foo"}));
	}

	@Test
	public void escapeURLEscapesDot() throws Throwable {
		String result = (String) P38.call("escapeURL", replican,
			new Object[]{"example.com"});
		assertEquals("example\\.com", result);
	}

	@Test
	public void escapeURLWithCaret() throws Throwable {
		assertEquals("\\^foo", P38.call("escapeURL", replican, new Object[]{"^foo"}));
	}

	@Test
	public void escapeURLWithBrackets() throws Throwable {
		String result = (String) P38.call("escapeURL", replican,
			new Object[]{"foo[bar]"});
		assertEquals("foo\\[bar\\]", result);
	}

	@Test
	public void escapeURLWithQuantifiers() throws Throwable {
		String result = (String) P38.call("escapeURL", replican,
			new Object[]{"foo*+?"});
		assertEquals("foo\\*\\+\\?", result);
	}

	@Test
	public void escapeURLWithPipe() throws Throwable {
		String result = (String) P38.call("escapeURL", replican,
			new Object[]{"a|b"});
		assertEquals("a\\|b", result);
	}
}
