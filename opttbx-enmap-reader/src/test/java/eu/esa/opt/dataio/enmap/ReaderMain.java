package eu.esa.opt.dataio.enmap;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

import java.io.File;
import java.io.IOException;

public class ReaderMain {

    public static void main(String[] args) throws IOException {
        Product product = ProductIO.readProduct(new File(args[0]), new EnmapProductReaderPlugIn().getFormatNames());
        Band band = product.getBandAt(12);
        band.readRasterDataFully(ProgressMonitor.NULL);
        System.out.println("band.getName() = " + band.getName());
    }
}
