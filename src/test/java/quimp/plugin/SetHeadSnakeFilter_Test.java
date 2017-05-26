package quimp.plugin;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.quimp.BOAState;
import uk.ac.warwick.wsbc.quimp.BOA_;
import uk.ac.warwick.wsbc.quimp.Node;
import uk.ac.warwick.wsbc.quimp.Snake;
import uk.ac.warwick.wsbc.quimp.utils.test.DataLoader;

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
   * Load resource file from either jar or filesystem.
   * 
   * <p>If class loader is an object run from jar, this method will make binary copy of resource in
   * temporary folder and return path to it.
   * 
   * <p>This code is taken from
   * https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
   * 
   * @param c class loader
   * @param resource resource name and relative path
   * @return path to resource file
   */
  public static Path loadResource(ClassLoader c, String resource) {
    File file = null;
    URL res = c.getResource(resource);
    if (res.toString().startsWith("jar:")) {
      try {
        InputStream input = c.getResourceAsStream(resource);
        file = File.createTempFile(new Date().getTime() + "", "");
        OutputStream out = new FileOutputStream(file);
        int read;
        byte[] bytes = new byte[1024];

        while ((read = input.read(bytes)) != -1) {
          out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
        input.close();
        file.deleteOnExit();
        return file.toPath();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      // this will probably work in your IDE, but not from a JAR
      return Paths.get(res.getFile());
    }
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
