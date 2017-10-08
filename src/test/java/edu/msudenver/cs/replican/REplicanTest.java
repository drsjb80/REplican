package edu.msudenver.cs.replican;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class REplicanTest {
	
	private REplican replican = new REplican();

	@Test
	public void testEscapeURL() throws Exception {
		assertEquals (P38.call("escapeURL", replican, new Object[]{"foo"}),	"foo");
		assertEquals (P38.call("escapeURL", replican, new Object[]{"^foo"}), "\\^foo");
	}
}
