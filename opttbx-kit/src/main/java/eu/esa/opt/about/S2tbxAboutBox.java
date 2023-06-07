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
import org.esa.snap.core.util.SystemUtils;
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
import java.awt.*;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Norman
 * Replaced by {@link OpttbxAboutBox}
 */
//@AboutBox(displayName = "S2TBX", position = 20)
@Deprecated
public class S2tbxAboutBox extends JPanel {

    private final static String releaseNotesUrlString = "https://senbox.atlassian.net/issues/?filter=-4&jql=project%20%3D%20SIITBX%20AND%20fixVersion%20%3D%20";


    public S2tbxAboutBox() {
        super(new BorderLayout(4, 4));
//        setBorder(new EmptyBorder(4, 4, 4, 4));
        ImageIcon aboutImage = new ImageIcon(S2tbxAboutBox.class.getResource("about_s2tbx.jpg"));

        add(createSeaDASVersionModPanel(), BorderLayout.WEST);

//        JLabel iconLabel = new JLabel(aboutImage);

//        add(iconLabel, BorderLayout.CENTER);
        add(createVersionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createSeaDASVersionModPanel() {

        String SEADAS_VERSION = "8.4.0";

        final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(S3tbxAboutBox.class);
        Version specVersion = Version.parseVersion(moduleInfo.getSpecificationVersion().toString());
        String versionString = String.format("%s.%s.%s", specVersion.getMajor(), specVersion.getMinor(), specVersion.getMicro());

        JLabel versionLabel = new JLabel("<html><b>Sentinel-3 Toolbox (S3TBX) version " + moduleInfo.getImplementationVersion() + "</b>", SwingConstants.CENTER);



//        String releaseNotesUrl = SystemUtils.getReleaseNotesUrl();
//        String seadasVersion = SystemUtils.getReleaseVersion();
//        String releaseNotesUrlName = "SeaDAS " + seadasVersion + " Release Notes";
//
//        mainPanel.add(getUrlJLabel(releaseNotesUrl, releaseNotesUrlName));


        JLabel infoText = new JLabel("<html>"
                + "<b>S2TBX implementation version: </b>" + moduleInfo.getImplementationVersion() + "* (SeaDAS Modified)<br>"
                + "<b>S2TBX git repository tag: </b>https://github.com/senbox-org/optical-toolbox/releases/tag/SEADAS-" + SEADAS_VERSION + "<br>"
                + "<br><hr>"
                + "</html>"
        );

        GridBagConstraints gbc = new GridBagConstraints();
        JPanel jPanel = new JPanel(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets.left = 5;
        gbc.insets.top = 5;

        gbc.gridy = 0;
        infoText.setMinimumSize(infoText.getPreferredSize());
        jPanel.add(infoText, gbc);


        ImageIcon aboutImage = new ImageIcon(S2tbxAboutBox.class.getResource("about_s2tbx.jpg"));
        JLabel iconLabel = new JLabel(aboutImage);

        gbc.gridy = 1;
        jPanel.add(iconLabel, gbc);

        return jPanel;
    }


    private JLabel getUrlJLabel(String url, String name) {
        final JLabel jLabel = new JLabel("<html> " +
                "<a href=\"" + url + "\">" + name + "</a></html>");
        jLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(url));
        return jLabel;
    }




    private JPanel createVersionPanel() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        int year = utc.get(Calendar.YEAR);
        JLabel copyRightLabel = new JLabel("<html><b>© 2014-" + year + " CS GROUP - France, CS GROUP - ROMANIA and contributors</b>", SwingConstants.CENTER);

        final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(S2tbxAboutBox.class);
        JLabel versionLabel = new JLabel("<html><b>Sentinel-2 Toolbox (S2TBX) version  " + moduleInfo.getImplementationVersion() + "* (SeaDAS Modified)</b>", SwingConstants.CENTER);

        Version specVersion = Version.parseVersion(moduleInfo.getSpecificationVersion().toString());
        String versionString = String.format("%s.%s.%s", specVersion.getMajor(), specVersion.getMinor(), specVersion.getMicro());
        String changelogUrl = releaseNotesUrlString + versionString;
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

}