/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2014-2015 CS-Romania (office@c-s.ro)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esa.opt.about;

import com.bc.ceres.core.runtime.Version;
import org.esa.snap.rcp.about.AboutBox;
import org.esa.snap.rcp.util.BrowserUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author DianaH
 */
@AboutBox(displayName = "Optical", position = 20)
public class OpttbxAboutBox extends JPanel {

    private final static String defaultReleaseNotesUrlString = "https://senbox.atlassian.net/jira/software/c/projects/SNAP/issues/?jql=project%20%3D%20%22SNAP%22%20AND%20component%20%3D%20Optical%20AND%20fixversion%20%3D%20"; // the version is appended in the code below
    private final static String stepReleaseNotesUrlString = "https://step.esa.int/main/wp-content/releasenotes/Optical/Optical_<version>.html"; // the version is appended in the code below

    public OpttbxAboutBox() {
        super(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        ImageIcon aboutImage = new ImageIcon(OpttbxAboutBox.class.getResource("about_opttbx.jpg"));
        JLabel iconLabel = new JLabel(aboutImage);
        add(iconLabel, BorderLayout.CENTER);
        add(createVersionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createVersionPanel() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        int year = utc.get(Calendar.YEAR);
        JLabel copyRightLabel = new JLabel("<html><b>© 2014-" + year + " Brockmann Consult GmbH, CS GROUP - France, CS GROUP - ROMANIA and contributors</b>", SwingConstants.CENTER);

        final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(OpttbxAboutBox.class);
        JLabel versionLabel = new JLabel("<html><b>Optical Toolbox version " + moduleInfo.getImplementationVersion() + "</b>", SwingConstants.CENTER);

        Version specVersion = Version.parseVersion(moduleInfo.getSpecificationVersion().toString());
        String versionString = String.format("%s.%s.%s", specVersion.getMajor(), specVersion.getMinor(), specVersion.getMicro());
        String changelogUrl = getReleaseNotesURLString(versionString);
        final JLabel releaseNoteLabel = new JLabel("<html><a href=\"" + changelogUrl + "\">Release Notes</a>", SwingConstants.CENTER);
        releaseNoteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        releaseNoteLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(changelogUrl));

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(copyRightLabel);
        mainPanel.add(versionLabel);
        mainPanel.add(releaseNoteLabel);
        return mainPanel;
    }

    private String getReleaseNotesURLString(String versionString){
        String changelogUrl = stepReleaseNotesUrlString.replace("<version>", versionString);
        try {
            URL url = new URL(changelogUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");

            int responseCode = huc.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                changelogUrl = defaultReleaseNotesUrlString + versionString;
            }
        } catch (IOException e) {
            changelogUrl = defaultReleaseNotesUrlString + versionString;
        }
        return changelogUrl;
    }
}
