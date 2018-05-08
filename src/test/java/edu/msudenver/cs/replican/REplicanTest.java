package edu.msudenver.cs.replican;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.InjectMocks;
import org.powermock.reflect.Whitebox;

import java.net.URL;


@RunWith(MockitoJUnitRunner.class)
public class REplicanTest {
	@Mock
	private REplican rep;
	@InjectMocks
	private REplicanArgs args;

    @Mock
    private Cookies cookies;
	@Before
	public void setUp() {
		rep = new REplican();
		args = new REplicanArgs();

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

		@Test
        public void testAddToURLS() throws Exception {


        }

	@Test
	public void testMatch() throws Exception {
		String a = "<base = http://game.pioneernet.ru/dl/q3/files.pk3";
		String b = "(<[bB][aA][sS][eE].*)([=]) [\\\"']?([^\\\"'# ]*)";
		String testMatch = Whitebox.invokeMethod(rep, "match", b, a);



		assertEquals("<base =http://game.pioneernet.ru/dl/q3/files.pk3", testMatch);
	}

    @Test
    public void testNewBase() throws Exception {

        //String testNull = Whitebox.invokeMethod(rep, "newBase", null);
        String b = "<a href=\"http://game.pioneernet.ru/dl/q3/files.pk3";
        String testBase = Whitebox.invokeMethod(rep, "newBase", b);

        assertEquals(null, testBase);

    }

    @Test
	public void interesting() throws Exception{

		 String testNull = Whitebox.invokeMethod(rep, "interesting", null);

		 assertEquals(null, testNull);
	}



	@Test
	public void testAddOne() throws Exception{

	}

	@Test
	public void testSetDefaults() throws Exception{
		/*String nullDefaults = Whitebox.invokeMethod(rep, "setDefaults");




		//String nullDefaults = Whitebox.invokeMethod(rep, "setDefaults");
		args.Interesting = null;


			String urlref = "\\s*=\\s*[\"']?([^\"'>]*)";
			String href = "[hH][rR][eE][fF]";
			String src = "[sS][rR][cC]";

			args.Interesting = new String[]{
					href + urlref,
					src + urlref,
			};

		assertEquals("[hH][rR][eE][fF]\\s*=\\s*[\"']?([^\"'>]*)",nullDefaults);
		*/
		}

		@Test
	public void testMakeURL() throws Exception {
		String baseURL = "http://game.pioneernet.ru/dl/q3/files.pk3";
		String testActualString = "/q3/";
		String testDefault = " ";

		URL testURL = Whitebox.invokeMethod(rep,"makeURL", baseURL,testActualString);
		URL testURL1 = Whitebox.invokeMethod(rep, "makeURL", baseURL,testDefault);
		URL testAS = new URL("http://game.pioneernet.ru/q3/");
		URL testD = new URL(baseURL);
		assertEquals(testAS,testURL );
		assertEquals(testD,testURL1);
		}


		@Test
	public void testProcess() throws Exception{

	}

	@Test
	public void testAddToURLs() throws Exception{

	}
}
