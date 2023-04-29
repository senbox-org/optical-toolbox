/* =========================================================================
 * This file is part of NITRO
 * =========================================================================
 * 
 * (C) Copyright 2004 - 2010, General Dynamics - Advanced Information Systems
 *
 * NITRO is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 */

package nitf.imageio;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class NITFReaderSpi extends ImageReaderSpi {
    private static final Log log = LogFactory.getLog(NITFReaderSpi.class);

    public NITFReaderSpi() {
        super("NITRO", // vendorName
              "1.0", // version
              new String[]{"NITF", "nitf", "NSIF"}, // names
              new String[]{"ntf", "nitf", "nsf"}, // suffixes
              new String[]{"image/x-ntf", "image/x-nitf"}, // MIMETypes
              NITFReader.class.getName(), // readerClassName
              new Class[]{File.class}, // inputTypes
              null, // writerSpiNames
              false, // supportsStandardStreamMetadataFormat
              null, // nativeStreamMetadataFormatName
              null, // nativeStreamMetadataFormatClassName
              null, // extraStreamMetadataFormatNames
              null, // extraStreamMetadataFormatClassNames
              false, // supportsStandardImageMetadataFormat
              null, // nativeImageMetadataFormatName
              null, // nativeImageMetadataFormatClassName
              null, // extraImageMetadataFormatNames
              null // extraImageMetadataFormatClassNames
        );
    }

    public static boolean isNITF(File file) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            byte[] firstFour = new byte[4];
            int bytesRead = fin.read(firstFour);
            if (bytesRead != 4) {
                return false;
            }
            return new String(firstFour).equals("NITF");
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (fin != null)
                    fin.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean canDecodeInput(Object source) {
        boolean result = source instanceof File;
        if (result)
            result = isNITF((File) source);
        return result;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new NITFReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "NITF 2.0/2.1 Reader";
    }

}
