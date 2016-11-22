package quimp.plugin;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.BOA_;
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
    static final Logger LOGGER = LoggerFactory.getLogger(SetHeadSnakeFilter_Test.class.getName());

    /**
     * Accessor to private fields
     * 
     * Example of use:
     * 
     * @code{.java} Snake s = new Snake(pr, 1); int ret = (int)
     *              accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s }, new
     *              Class[] { Snake.class });
     * @endcode
     * 
     * @param name Name of private method
     * @param obj Reference to object
     * @param param Array of parameters if any
     * @param paramtype Array of classes of \c param
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    static Object accessPrivate(String name, SetHeadSnakeFilter_ obj, Object[] param,
            Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
        prv.setAccessible(true);
        return prv.invoke(obj, param);
    }

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
        BOA_.qState = new BOAState();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFindNearestToBoundingBox() throws Exception {
        PolygonRoi pr = new PolygonRoi(d1.getFloatPolygon(), Roi.POLYGON);
        Snake s = new Snake(pr, 1);
        int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
                new Class[] { Snake.class });
        assertEquals(1, ret);
    }

    @Test
    public void testFindNearestToBoundingBox_1() throws Exception {
        PolygonRoi pr = new PolygonRoi(d2.getFloatPolygon(), Roi.POLYGON);
        LOGGER.debug(d2.toString());
        Snake s = new Snake(pr, 1);
        int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
                new Class[] { Snake.class });
        assertEquals(4, ret);
    }

    @Test
    public void testFindNearestToBoundingBox_2() throws Exception {
        PolygonRoi pr = new PolygonRoi(d3.getFloatPolygon(), Roi.POLYGON);
        LOGGER.debug(d3.toString());
        Snake s = new Snake(pr, 1);
        int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
                new Class[] { Snake.class });
        assertEquals(5, ret);
    }

}
