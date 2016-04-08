
/**
 * @file SetHeadSnakeFilter_.java
 * @date 4 Apr 2016
 */

import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.Node;
import uk.ac.warwick.wsbc.QuimP.Snake;
import uk.ac.warwick.wsbc.QuimP.ViewUpdater;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpSnakeFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QWindowBuilder;

/**
 * Implements filter that change first node of Snake object
 * 
 * @author p.baniukiewicz
 * @date 4 Apr 2016
 *
 */
public class SetHeadSnakeFilter_ extends QWindowBuilder implements IQuimpSnakeFilter,
        IQuimpPluginSynchro, ChangeListener, ActionListener, ItemListener {

    static {
        System.setProperty("log4j.configurationFile", "setheadsnakefilterlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(SetHeadSnakeFilter_.class.getName());
    private ParamList uiDefinition; //!< Definition of UI
    protected ViewUpdater qcontext; //!< remember QuimP context to recalculate and update its view
    private String method;
    private Snake snake;
    
    /**
     * 
     */
    public SetHeadSnakeFilter_() {
        // create UI using QWindowBuilder
        uiDefinition = new ParamList(); // will hold ui definitions
        // configure window, names of UI elements are also names of variables
        // exported/imported by set/getPluginConfig
        uiDefinition.put("name", "SetHeadSnakeFilter"); // name of win
        uiDefinition.put("method", "choice, minX, minCentroid");
        uiDefinition.put("help", "");
        buildWindow(uiDefinition); // construct ui (not shown yet)
    }

    @Override
    public int setup() {
        return DOES_SNAKES + MODIFY_INPUT;
    }

    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        // TODO Auto-generated method stub

    }

    @Override
    public ParamList getPluginConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void showUI(boolean val) {
        toggleWindow(val);
    }

    @Override
    public String getVersion() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public Snake runPlugin() throws QuimpPluginException {
        method = getStringFromUI("method");
        LOGGER.debug(String.format("Run plugin with params: method %s", method));
        int count = 0;
        int pos = count;
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
                    LOGGER.debug("node: " + n.toString());
                    n = n.getNext();
                    count++;
                } while (!n.isHead());
                // go to pos node
                n = snake.getHead();
                Node oldhead = n;
                do {
                    n = n.getNext();
                } while (n.getTrackNum() != pos && !n.isHead());
                n.setHead(true);
                oldhead.setHead(false);
                snake.findHead();
                break;
            }
            case "minCentroid": {
                ExtendedVector2d c = snake.getCentroid();
                double mindist = Double.MAX_VALUE;
                do {
                    ExtendedVector2d p = new ExtendedVector2d(n.getX(), n.getY()); // vec from node
                    p.sub(c); // vector node - centroid
                    double len = p.length(); // distance between node and centroid
                    if (len < mindist) {
                        mindist = len;
                        pos = n.getTrackNum();
                        ;
                    }
                    LOGGER.debug("Distance: " + len + " between " + n.toString());
                    n = n.getNext();
                    count++;
                } while (!n.isHead());
                // go to pos node
                n = snake.getHead();
                Node oldhead = n;
                do {
                    n = n.getNext();
                } while (n.getTrackNum() != pos && !n.isHead());
                n.setHead(true);
                oldhead.setHead(false);
                snake.findHead();
                break;
            }
            default:
                throw new QuimpPluginException("Method not supported. check config data");
        }
        return snake;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        if (b == applyB) { // pressed apply, copy ui data to plugin
            qcontext.updateView();
            LOGGER.debug("actionPerformed called");
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        if (isWindowVisible() == true) {
            qcontext.updateView();
            LOGGER.debug("stateChanged called");
        }

    }

    @Override
    public void buildWindow(final ParamList def) {
        super.buildWindow(def); // window must be built first
        // attach listener to selected ui
        ((Choice) ui.get("method")).addItemListener(this);
        applyB.addActionListener(this); // attach listener to apply button
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (isWindowVisible() == true) {
            qcontext.updateView();
            LOGGER.debug("stateChanged called");
        }
    }

}
