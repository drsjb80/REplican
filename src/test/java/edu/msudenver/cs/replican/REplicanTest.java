package edu.msudenver.cs.replican;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class REplicanTest {
	
	private final REplican replican = new REplican();

	@Test
	public void testEscapeURL() throws Throwable {
		assertEquals (P38.call("escapeURL", replican, new Object[]{"foo"}),	"foo");
		assertEquals (P38.call("escapeURL", replican, new Object[]{"^foo"}), "\\^foo");
	}
}
