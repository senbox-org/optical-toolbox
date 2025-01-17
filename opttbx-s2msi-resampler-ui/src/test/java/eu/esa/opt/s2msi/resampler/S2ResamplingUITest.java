package eu.esa.opt.s2msi.resampler;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.binding.dom.XppDomElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.core.gpf.graph.Node;
import org.esa.snap.core.util.DefaultPropertyMap;
import org.esa.snap.core.util.PropertyMap;
import org.esa.snap.graphbuilder.rcp.dialogs.support.GraphNode;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.product.ProductSceneView;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static org.junit.Assert.*;

public class S2ResamplingUITest {

    private S2ResamplingUI s2ResamplingUI;
    private AppContext appContext;

    @Before
    public void setUp() throws Exception {
        s2ResamplingUI = new S2ResamplingUI();
        appContext = new MockAppContext();
    }

    @Test
    @STTM("SNAP-3794")
    public void testReadGraphNode() {
        final Node node = new Node("S2Resampling", "S2Resampling");
        final XppDomElement parameters = new XppDomElement("parameters");
        node.setConfiguration(parameters);

        final GraphNode graphNode = new GraphNode(node);
        final Map<String, Object> paramMap = graphNode.getParameterMap();
        paramMap.put("resolution", "10"); // default value is "60", actual name is 'targetResolution'

        JComponent component = s2ResamplingUI.CreateOpTab(graphNode.getOperatorName(), graphNode.getParameterMap(), appContext);
        assertNotNull(component);

        assertEquals("10", s2ResamplingUI.getPropertySet().getProperty("targetResolution").getValue());
    }

    private static class MockAppContext implements AppContext {
        private final PropertyMap preferences = new DefaultPropertyMap();
        private final ProductManager prodMan = new ProductManager();

        public Window getApplicationWindow() {
            return null;
        }

        public String getApplicationName() {
            return "Killer App";
        }

        public Product getSelectedProduct() {
            return null;
        }

        public void handleError(Throwable e) {
            JOptionPane.showMessageDialog(getApplicationWindow(), e.getMessage());
        }

        public void handleError(String message, Throwable e) {
            JOptionPane.showMessageDialog(getApplicationWindow(), message);
        }

        public PropertyMap getPreferences() {
            return preferences;
        }

        public ProductManager getProductManager() {
            return prodMan;
        }

        public ProductSceneView getSelectedProductSceneView() {
            return null;
        }
    }
}
