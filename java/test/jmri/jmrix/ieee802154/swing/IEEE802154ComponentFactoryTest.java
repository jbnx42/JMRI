package jmri.jmrix.ieee802154.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IEEE802154ComponentFactoryTest {

    @Test
    public void testCTor() {
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo = new jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo();
        IEEE802154ComponentFactory t = new IEEE802154ComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IEEE802154ComponentFactoryTest.class);

}
