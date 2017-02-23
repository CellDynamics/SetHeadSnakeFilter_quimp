
/**
 * @file SetHeadSnakeFilter_.java
 * @date 4 Apr 2016
 */
package quimp.plugin;

import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.Node;
import uk.ac.warwick.wsbc.quimp.PropertyReader;
import uk.ac.warwick.wsbc.quimp.Snake;
import uk.ac.warwick.wsbc.quimp.ViewUpdater;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.plugin.snakes.IQuimpBOASnakeFilter;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QWindowBuilder;

/**
 * Implements filter that change first node of Snake object.
 * 
 * This filter modify of input data passed as reference. The following methods are currently
 * supported: -# minX -# nearestCentroid -# minXY
 * 
 * The first method \b minX selects new head node as node that has smallest \c X coordinate.
 * 
 * The second method \b nearestCentroid calculates distance between every node and Snake centroid
 * and selects as head node the node that is closest to centroid.
 * 
 * The third method \b minXY evaluates bounding box of Snake. Then as head node the point that is
 * closest to left upper corner of bounding box is set.
 * 
 * @author p.baniukiewicz
 * @date 4 Apr 2016
 *
 */
public class SetHeadSnakeFilter_ extends QWindowBuilder implements IQuimpBOASnakeFilter,
        IQuimpPluginSynchro, ChangeListener, ActionListener, ItemListener {

    static final Logger LOGGER = LoggerFactory.getLogger(SetHeadSnakeFilter_.class.getName());
    private ParamList uiDefinition; /*!< Definition of UI */
    protected ViewUpdater qcontext; /*!< remember QuimP context to recalculate and update its view*/
    private String method; /*!< method of choosing head node as entry in Choice selector */
    private Snake snake; /*!< reference to input data */

    /**
     * Construct GUI window
     */
    public SetHeadSnakeFilter_() {
        // create UI using QWindowBuilder
        uiDefinition = new ParamList(); // will hold ui definitions
        // configure window, names of UI elements are also names of variables
        // exported/imported by set/getPluginConfig
        uiDefinition.put("name", "SetHeadSnakeFilter"); // name of win
        uiDefinition.put("method", "choice, minX, nearestCentroid, minXY");
        uiDefinition.put("help",
                "Short description:\nminX - set head to point with smallest X coordinate"
                        + "\nnearestCentroid - set head to point which is closest to centroid point"
                        + "\nminXY - set head to point which is closest to corner of bounding box");
        buildWindow(uiDefinition); // construct ui (not shown yet)
    }

    @Override
    public int setup() {
        return DOES_SNAKES + MODIFY_INPUT;
    }

    /**
     * Configure plugin and overrides default values.
     * 
     * Supported keys:
     * <ol>
     * <li>\c method - method of choosing head node
     * </ol>
     * 
     * @param par configuration as pairs <key,val>. Keys are defined by plugin creator and plugin
     *        caller do not modify them.
     * @throws QuimpPluginException on wrong parameters list or wrong parameter conversion
     * @see uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin#setPluginConfig(ParamList)
     */
    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        try {
            method = par.getStringValue("method");
            setValues(par); // copy incoming parameters to UI
        } catch (Exception e) {
            // we should never hit this exception as parameters are not touched by caller they are
            // only passed to configuration saver and restored from it
            throw new QuimpPluginException("Wrong input argument->" + e.getMessage(), e);
        }

    }

    @Override
    public ParamList getPluginConfig() {
        return getValues();
    }

    @Override
    public int showUi(boolean val) {
        toggleWindow(val);
        return 0;
    }

    @Override
    public String getVersion() {
        String trimmedClassName = getClass().getSimpleName();
        trimmedClassName = trimmedClassName.substring(0, trimmedClassName.length() - 1); // no _
        // _ at the end of class does not appears in final jar name, we need it to
        // distinguish between plugins
        return PropertyReader.readProperty(getClass(), trimmedClassName,
                "quimp/plugin/plugin.properties", "internalVersion");
    }

    /**
     * Main plugin runner
     * 
     */
    @Override
    public Snake runPlugin() throws QuimpPluginException {
        // TODO Clean this method moving cases to separate functions
        method = getStringFromUI("method");
        LOGGER.debug(String.format("Run plugin with params: method %s", method));
        int pos = 1;
        Node n = snake.getHead();
        switch (method) {
            case "minX": {
                // change tp asPolygon, and get bounds
                double minX = n.getX();
                n = n.getNext();
                do {
                    if (n.getX() < minX) {
                        minX = n.getX();
                        pos = n.getTrackNum();
                    }
                    LOGGER.trace("node: " + n.toString());
                    n = n.getNext();
                } while (!n.isHead());
                // go to pos node
                snake.setNewHead(pos);
                break;
            }
            case "nearestCentroid": {
                ExtendedVector2d c = snake.getCentroid();
                double mindist = Double.MAX_VALUE;
                do {
                    ExtendedVector2d p = new ExtendedVector2d(n.getX(), n.getY()); // vec from node
                    p.sub(c); // vector node - centroid
                    double len = p.length(); // distance between node and centroid
                    if (len < mindist) {
                        mindist = len;
                        pos = n.getTrackNum();
                    }
                    LOGGER.trace("Distance: " + len + " between " + n.toString());
                    n = n.getNext();
                } while (!n.isHead());
                // go to pos node
                snake.setNewHead(pos);
                break;
            }
            case "minXY": {
                pos = findNearestToBoundingBox(snake);
                // go to pos node
                snake.setNewHead(pos);
                break;
            }
            default:
                throw new QuimpPluginException("Method not supported. check config data");
        }
        return snake;
    }

    /**
     * Return point which is nearest to lower left point of bounding box.
     * 
     * @param s Snake to be analyzed
     * @return Index of Snake Node which is closest to considered point. Nodes are numbered from 1
     */
    private int findNearestToBoundingBox(Snake s) {
        Rectangle2D.Double bounds = s.getDoubleBounds();
        LOGGER.debug("Rectangle pos: " + bounds.getX() + " " + bounds.getY());
        Point2d p0 = new Point2d(bounds.getX(), bounds.getY());
        // calculate lengths
        Node n = s.getHead();
        int pos = 1;
        double len;
        double minlen = Double.MAX_VALUE;
        do {
            Vector2d v = new Vector2d(n.getX(), n.getY());
            v.sub(p0);
            len = v.length();
            if (len < minlen) {
                minlen = len;
                pos = n.getTrackNum();
            }
            LOGGER.trace("Distance: " + len + " between " + n.toString());
            n = n.getNext();
        } while (!n.isHead());
        return pos;
    }

    @Override
    public void attachData(Snake data) {
        if (data == null)
            return;
        snake = data;
    }

    @Override
    public void attachContext(ViewUpdater b) {
        qcontext = b;
    }

    /**
     * Override window builder to add listeners to GUI
     */
    @Override
    public void buildWindow(final ParamList def) {
        super.buildWindow(def); // window must be built first
        // attach listener to selected ui
        ((Choice) ui.get("method")).addItemListener(this);
        applyB.addActionListener(this); // attach listener to apply button
    }

    /**
     * Update view on any change in GUI
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        if (b == applyB) { // pressed apply, copy ui data to plugin
            qcontext.updateView();
            LOGGER.debug("actionPerformed called");
        }
    }

    /**
     * Update view on any change in GUI
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        if (isWindowVisible() == true) {
            qcontext.updateView();
            LOGGER.debug("stateChanged called");
        }

    }

    /**
     * Update view on any change in GUI
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (isWindowVisible() == true) {
            qcontext.updateView();
            LOGGER.debug("stateChanged called");
        }
    }

    @Override
    public String about() {
        return "Select head node in outline\nAuthor: Piotr Baniukiewicz\n"
                + "mail: p.baniukiewicz@warwick.ac.uk";
    }

}
