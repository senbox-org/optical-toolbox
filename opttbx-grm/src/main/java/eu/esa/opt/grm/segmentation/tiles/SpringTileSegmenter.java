package eu.esa.opt.grm.segmentation.tiles;

import eu.esa.opt.grm.RegionMergingProcessingParameters;
import eu.esa.opt.grm.segmentation.BoundingBox;
import eu.esa.opt.grm.segmentation.Contour;
import eu.esa.opt.grm.segmentation.Node;
import eu.esa.opt.grm.segmentation.SpringNode;
import eu.esa.opt.grm.segmentation.SpringSegmenter;
import eu.esa.opt.grm.segmentation.*;
import org.esa.snap.utils.BufferedInputStreamWrapper;
import org.esa.snap.utils.BufferedOutputStreamWrapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Jean Coravu
 */
public class SpringTileSegmenter extends AbstractTileSegmenter {

    public SpringTileSegmenter(RegionMergingProcessingParameters processingParameters, int totalIterationsForSecondSegmentation,
                               float threshold, boolean fastSegmentation, Path temporaryParentFolder)
                               throws IOException {

        super(processingParameters, totalIterationsForSecondSegmentation, threshold, fastSegmentation, temporaryParentFolder);
    }

    @Override
    protected SpringNode buildNode(int nodeId, BoundingBox box, Contour contour, int perimeter, int area, int numberOfComponentsPerPixel) {
        return new SpringNode(nodeId, box, contour, perimeter, area, numberOfComponentsPerPixel);
    }

    @Override
    public AbstractSegmenter buildSegmenter(float threshold) {
        return new SpringSegmenter(threshold);
    }

    @Override
    protected void writeNode(BufferedOutputStreamWrapper nodesFileStream, Node nodeToWrite) throws IOException {
        super.writeNode(nodesFileStream, nodeToWrite);

        SpringNode node = (SpringNode)nodeToWrite;
        int count = node.getNumberOfComponentsPerPixel();
        for (int i=0; i<count; i++) {
            nodesFileStream.writeFloat(node.getMeansAt(i));
        }
    }

    @Override
    protected Node readNode(BufferedInputStreamWrapper nodesFileStream) throws IOException {
        SpringNode node = (SpringNode)super.readNode(nodesFileStream);

        int count = node.getNumberOfComponentsPerPixel();
        for (int i=0; i<count; i++) {
            node.setMeansAt(i, nodesFileStream.readFloat());
        }

        return node;
    }
}
