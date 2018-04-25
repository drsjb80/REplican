package edu.msudenver.cs.replican;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.InjectMocks;
import org.powermock.reflect.Whitebox;

import java.awt.*;

@RunWith(MockitoJUnitRunner.class)
public class REplicanTest {

	private REplican rep;

    @Mock
    private Cookies cookies;
	@Before
	public void setUp() {
		rep = new REplican();
	}
    @Test
	public void testEscapeURL() throws Exception {
		String od = "foo";
		String oc = "\\^foo";
		String ov = "\\^foo\\*bar";

		String actual = Whitebox.invokeMethod( rep, "escapeURL", "foo" );
		String actual1 = Whitebox.invokeMethod( rep, "escapeURL", "^foo" );
		String actual2 = Whitebox.invokeMethod( rep, "escapeURL", "^foo*bar" );
		assertEquals(od, actual);
		assertEquals(oc,actual1);
		assertEquals(ov,actual2);
		}



}
