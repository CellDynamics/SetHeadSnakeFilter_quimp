import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.Snake;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.DataLoader;

/**
 * @file SetHeadSnakeFilter_Test.java
 * @date 10 Apr 2016
 */

/**
 * @author p.baniukiewicz
 * @date 10 Apr 2016
 *
 */
public class SetHeadSnakeFilter_Test {
    static {
        System.setProperty("log4j.configurationFile", "setheadsnakefilterlog4j2.xml");
    }
    private static final Logger LOGGER =
            LogManager.getLogger(SetHeadSnakeFilter_Test.class.getName());

    private DataLoader d1, d2, d3;
    private SetHeadSnakeFilter_ testobj;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testobj = new SetHeadSnakeFilter_();
        d1 = new DataLoader("src/test/resources/test1.txt");
        d2 = new DataLoader("src/test/resources/test2.txt");
        d3 = new DataLoader("src/test/resources/test3.txt");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFindNearest() throws Exception {
        PolygonRoi pr = new PolygonRoi(d1.getFloatPolygon(), Roi.POLYGON);
        Snake s = new Snake(pr, 1);
        int ret = testobj.findNearest(s);
        assertEquals(1, ret);
    }

    @Test
    public void testFindNearest_1() throws Exception {
        PolygonRoi pr = new PolygonRoi(d2.getFloatPolygon(), Roi.POLYGON);
        LOGGER.debug(d2.toString());
        Snake s = new Snake(pr, 1);
        int ret = testobj.findNearest(s);
        assertEquals(4, ret);
    }

    @Test
    public void testFindNearest_2() throws Exception {
        PolygonRoi pr = new PolygonRoi(d3.getFloatPolygon(), Roi.POLYGON);
        LOGGER.debug(d3.toString());
        Snake s = new Snake(pr, 1);
        int ret = testobj.findNearest(s);
        assertEquals(5, ret);
    }

}
