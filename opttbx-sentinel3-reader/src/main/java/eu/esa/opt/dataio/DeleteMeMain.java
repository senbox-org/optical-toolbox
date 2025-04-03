package eu.esa.opt.dataio;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

import java.io.IOException;

public class DeleteMeMain {

    public static void main(String[] args) throws IOException {
        try (Product product = ProductIO.readProduct("C:\\Satellite\\Sentinel-3\\OLCI\\L1b\\S3B_OL_1_EFR____20231214T092214_20231214T092514_20231214T204604_0179_087_207_2340_PS2_O_NT_003.SEN3\\xfdumanifest.xml")) {
            MetadataElement metadataRoot = product.getMetadataRoot();
           printElementsAndAttributes(metadataRoot);

            MetadataElement referencePressureLevel = metadataRoot.getElement("reference_pressure_level");
            printElementsAndAttributes(referencePressureLevel);
        }
    }

    private static void printElementsAndAttributes(MetadataElement parentElement) {
        printAttributes(parentElement);
        for (MetadataElement childElement : parentElement.getElements()) {
            printAttributes(childElement);
        }
    }

    private static void printAttributes(MetadataElement element) {
        final MetadataAttribute[] attributes = element.getAttributes();
        for (MetadataAttribute attribute : attributes) {
            System.out.println("attribute = " + attribute);
        }
    }
}
