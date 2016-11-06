package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig;
import jmri.jmrix.can.adapters.gridconnect.net.NetworkDriverAdapter;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;

/**
 * Handle XML persistance of layout connections by persistening the
 * NetworkDriverAdapter (and connections).
 * <P>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = new NetworkDriverAdapter();
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
