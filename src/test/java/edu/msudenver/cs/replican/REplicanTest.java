package edu.msudenver.cs.replican;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.InjectMocks;

@RunWith(MockitoJUnitRunner.class)
public class REplicanTest {
	
	private final REplican replican = new REplican();
    @Mock
    private Cookies cookies;

    @Test
	public void testEscapeURL() throws Exception {
		assertEquals (P38.call("escapeURL", replican, new Object[]{"foo"}),	"foo");
		assertEquals (P38.call("escapeURL", replican, new Object[]{"^foo"}), "\\^foo");
	}
}
