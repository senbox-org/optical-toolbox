package eu.esa.opt.slstr.pdu.stitching.manifest;

import eu.esa.opt.slstr.pdu.stitching.PDUStitchingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Date;
import java.util.List;

/**
 * @author Tonio Fincke
 */
class StopTimesMerger extends AbstractElementMerger {

    @Override
    public void mergeNodes(List<Node> fromParents, Element toParent, Document toDocument) throws PDUStitchingException {
        String latetDateAsNodeValue = fromParents.get(0).getFirstChild().getNodeValue();
        Date latestDate = parseDate(latetDateAsNodeValue);
        if (fromParents.size() > 1) {
            for (int i = 1; i < fromParents.size(); i++) {
                final String nodeValue = fromParents.get(i).getFirstChild().getNodeValue();
                final Date date = parseDate(nodeValue);
                if (date.after(latestDate)) {
                    latetDateAsNodeValue = nodeValue;
                    latestDate = date;
                }
            }
        }
        addTextToNode(toParent, latetDateAsNodeValue, toDocument);
    }

}
