package edu.msudenver.cs.replican;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;


@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {
    @Mock
    private Utils util;

    @Mock
    private Cookies cookies;
    @Before
    public void setUp() {
        util = new Utils();
    }

    @Test
    public void testReplaceAll() throws Exception {

        String t = "<a href=\"" + "http://game.pioneernet.ru/dl/q3/files/pk3/ztn3tourney1.pk3" + "\">";
        String r = "<a href=\"" + "http://game.pioneernet.ru/dl/q3/files.pk3.ztn3tourney1" + "\">";
        //pairs[0] == "\\.wmv.*" and pairs[1] == ".wmv"
        String [] pairs = {"\\.pk3.*",".pk3"};

        String test = Whitebox.invokeMethod(util, "replaceAll",t,pairs );
        String test1 = Whitebox.invokeMethod(util, "replaceAll",r,pairs );

        assertEquals("<a href=\"http://game.pioneernet.ru/dl/q3/files/pk3/ztn3tourney1.pk3",test);
        assertEquals("<a href=\"http://game.pioneernet.ru/dl/q3/files.pk3",test1);


    }

    @Test
    public void testMatches() throws Exception{
        String [] testRe = {".*\\.pk3"};
        String testString = "http://game.pioneernet.ru/dl/q3/files.pk3";

        boolean tester = Whitebox.invokeMethod(util, "matches",testRe,testString);

        assertEquals(true,tester);
    }
}
