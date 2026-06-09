package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class REplicanTest {

	private final REplican replican = new REplican();

	@BeforeEach
	void setUp() {
		// Reset static state before each test
		REplican.ARGS.Interesting = null;
		REplican.ARGS.URLFixUp = null;
		REplican.ARGS.MIMEExamine = null;
		REplican.ARGS.MIMEIgnore = null;
		REplican.ARGS.PathExamine = null;
		REplican.ARGS.PathIgnore = null;
		REplican.ARGS.MIMESave = null;
		REplican.ARGS.MIMERefuse = null;
		REplican.ARGS.PathSave = null;
		REplican.ARGS.PathRefuse = null;
		REplican.ARGS.PathAccept = null;
		REplican.ARGS.PathReject = null;
		REplican.ARGS.PrintAll = false;
		REplican.ARGS.PrintAccept = false;
		REplican.ARGS.PrintReject = false;
		REplican.ARGS.PrintExamine = false;
		REplican.ARGS.PrintSave = false;
		REplican.ARGS.PrintRefuse = false;
		REplican.ARGS.additional = null;
	}

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

	@Test
	public void setDefaultsWithInterestingPatterns() throws Throwable {
		REplican.ARGS.additional = new String[]{"http://example.com"};
		P38.call("setDefaults", replican, new Object[]{});

		assertNotNull(REplican.ARGS.Interesting);
		assertEquals(2, REplican.ARGS.Interesting.length);
		assertTrue(REplican.ARGS.Interesting[0].contains("[hH][rR][eE][fF]"));
		assertTrue(REplican.ARGS.Interesting[1].contains("[sS][rR][cC]"));
	}

	@Test
	public void setDefaultsWithURLFixUp() throws Throwable {
		REplican.ARGS.additional = new String[]{"http://example.com"};
		P38.call("setDefaults", replican, new Object[]{});

		assertNotNull(REplican.ARGS.URLFixUp);
		assertTrue(REplican.ARGS.URLFixUp.length > 0);
	}

	@Test
	public void setDefaultsWithMIMEExamine() throws Throwable {
		REplican.ARGS.additional = new String[]{"http://example.com"};
		P38.call("setDefaults", replican, new Object[]{});

		assertNotNull(REplican.ARGS.MIMEExamine);
		assertEquals(1, REplican.ARGS.MIMEExamine.length);
		assertEquals("text/.*", REplican.ARGS.MIMEExamine[0]);
	}

	@Test
	public void setDefaultsWithPathSave() throws Throwable {
		REplican.ARGS.additional = new String[]{"http://example.com", "http://example2.com"};
		P38.call("setDefaults", replican, new Object[]{});

		assertNotNull(REplican.ARGS.PathSave);
		assertEquals(2, REplican.ARGS.PathSave.length);
	}
}
