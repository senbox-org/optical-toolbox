package eu.esa.opt.c2rcc.ancillary;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class AncRepository {

    private final Path repsitoryRoot;
    private final AncDownloader ancDownloader;
    private final Map<String, Product> productMap;

    public AncRepository(Path repsitoryRoot) {
        this(repsitoryRoot, null);
    }

    public AncRepository(Path repsitoryRoot, AncDownloader ancDownloader) {
        this.repsitoryRoot = repsitoryRoot;
        this.ancDownloader = ancDownloader;
        productMap = new HashMap<>();
    }

    public Product getProduct(String[] filenames) throws IOException {
        Product product;
        product = findProductInMap(filenames);
        if (product != null) {
            return product;
        }
        final Path[] productFiles = createProductFiles(filenames);
        product = findProductInArchive(productFiles);
        if (product != null) {
            return product;
        }
        if (ancDownloader == null) {
            return null;
        }
        final Path productFile = ancDownloader.download(productFiles);
        if (productFile == null) {
            return null;
        }
        return loadProduct(productFile);
    }

    private Product findProductInArchive(Path[] productFiles) throws IOException {
        for (Path productFile : productFiles) {
            if (Files.exists(productFile)) {
                return loadProduct(productFile);
            }
        }
        return null;
    }

    private Product loadProduct(Path productFile) throws IOException {
        final String filename = productFile.getFileName().toString();
        final Product product = ProductIO.readProduct(productFile);
        if (product != null) {
            productMap.put(filename, product);
        }
        return product;
    }

    private Path[] createProductFiles(String[] filenames) {
        final Path[] productFiles = new Path[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            String filename = filenames[i];
            if (!filename.matches("[N][12][09]\\d{2}[0-3]\\d{2}[0-2]\\d_.*")) {
                throw new IllegalArgumentException(filename + " is not a valid ancillary filename.");
            }
            final String year = filename.substring(1, 5);
            final String doy = filename.substring(5, 8);
            final Path doyPath = repsitoryRoot.resolve(year).resolve(doy);
            final Path productFile = doyPath.resolve(filename);
            productFiles[i] = productFile;
        }
        return productFiles;
    }

    private Product findProductInMap(String[] filenames) {
        for (String filename : filenames) {
            if (productMap.containsKey(filename)) {
                return productMap.get(filename);
            }
        }
        return null;
    }

    public void dispose() {
        for (Product product : productMap.values()) {
            product.dispose();
        }
    }
}