package quimp.plugin;

import static com.github.baniuk.ImageJTestSuite.dataaccess.ResourceLoader.loadResource;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.baniuk.ImageJTestSuite.dataaccess.DataLoader;
import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.Node;
import com.github.celldynamics.quimp.Snake;

import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SetHeadSnakeFilter_Test {
  static final Logger LOGGER = LoggerFactory.getLogger(SetHeadSnakeFilter_Test.class.getName());

  /**
   * Accessor to private fields
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
   * @return Private method
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
    d1 = new DataLoader(loadResource(getClass().getClassLoader(), "test1.txt").toString());
    d2 = new DataLoader(loadResource(getClass().getClassLoader(), "test2.txt").toString());
    d3 = new DataLoader(loadResource(getClass().getClassLoader(), "test3.txt").toString());
    BOA_.qState = new BOAState();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRunPlugin() throws Exception {
    SetHeadSnakeFilter_ tmpo = Mockito.spy(new SetHeadSnakeFilter_());
    Mockito.when(tmpo.getStringFromUI("method")).thenReturn("minX");
    PolygonRoi pr = new PolygonRoi(d3.getFloatPolygon(), Roi.POLYGON);
    Snake s = new Snake(pr, 1);
    tmpo.attachData(s);
    tmpo.runPlugin();
    Node h = s.getHead();
    assertEquals(3, h.getX(), 1e-5);
    assertEquals(6, h.getY(), 1e-5);

    Mockito.when(tmpo.getStringFromUI("method")).thenReturn("minXY");
    s = new Snake(pr, 1);
    tmpo.attachData(s);
    tmpo.runPlugin();
    h = s.getHead();
    assertEquals(3.5, h.getX(), 1e-5);
    assertEquals(2.5, h.getY(), 1e-5);

    Mockito.when(tmpo.getStringFromUI("method")).thenReturn("nearestCentroid");
    s = new Snake(pr, 1);
    tmpo.attachData(s);
    tmpo.runPlugin();
    h = s.getHead();
    assertEquals(3.0, h.getX(), 1e-5);
    assertEquals(3.0, h.getY(), 1e-5);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFindNearestToBoundingBox() throws Exception {
    PolygonRoi pr = new PolygonRoi(d1.getFloatPolygon(), Roi.POLYGON);
    Snake s = new Snake(pr, 1);
    int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
            new Class[] { Snake.class });
    assertEquals(1, ret);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFindNearestToBoundingBox_1() throws Exception {
    PolygonRoi pr = new PolygonRoi(d2.getFloatPolygon(), Roi.POLYGON);
    LOGGER.debug("testFindNearestToBoundingBox_1" + d2.toString());
    Snake s = new Snake(pr, 1);
    // dirty hack - move head if it was set to last node due to removing fake head in Snake
    // constructor
    Node n = s.getHead();
    if (n.getX() == 4) {
      s.setHead(n.getNext().getTrackNum());
    }
    int ret = (int) accessPrivate("findNearestToBoundingBox", testobj, new Object[] { s },
            new Class[] { Snake.class });
    assertEquals(4, ret);
  }

  /**
   * @throws Exception
   */
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
