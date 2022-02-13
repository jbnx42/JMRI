package jmri.jmrix.openlcb;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.openlcb.EventID;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import jmri.util.NamedBeanComparator;
import jmri.util.PropertyChangeListenerScaffold;
import jmri.util.ThreadingUtil;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensor class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private final static Logger log = LoggerFactory.getLogger(OlcbSensorTest.class);
    protected PropertyChangeListenerScaffold l;

    @Override
    public int numListeners() {
        return 0;
    }

    @Override
    public void checkActiveMsgSent() {
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(ti.tc.rcvMessage));
    }

    @Override
    public void checkInactiveMsgSent() {
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(ti.tc.rcvMessage));
    }

    @org.junit.jupiter.api.Disabled("Test requires further setup")
    @jmri.util.junit.annotations.ToDo("Check checkActiveMsgSent() producing correct result")
    @Test
    @Override
    public void testCommandSentActive() {}

    @org.junit.jupiter.api.Disabled("Test requires further setup")
    @jmri.util.junit.annotations.ToDo("Check checkInactiveMsgSent() producing correct result")
    @Test
    @Override
    public void testCommandSentInactive() {}

    @Override
    public void checkStatusRequestMsgSent() {
        ti.flush();
        ti.assertSentMessage(":X198F4C4CN0102030405060708;");
    }

    @Test
    public void testIncomingChange() {
        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4123
        );
        mActive.setExtended(true);

        CanMessage mInactive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 9},
                0x195B4123
        );
        mInactive.setExtended(true);

        // check states
        Assert.assertEquals(Sensor.UNKNOWN, t.getKnownState());

        ti.sendMessage(mActive);
        Assert.assertEquals(Sensor.ACTIVE,t.getKnownState());

        ti.sendMessage(mInactive);
        Assert.assertEquals(Sensor.INACTIVE, t.getKnownState());

    }

    @Test
    public void testRecoverUponStartup() {
        ti.flush();

        Assert.assertNotNull(ti.tc.rcvMessage);
        log.debug("recv msg: {} header {}", ti.tc.rcvMessage, Integer.toHexString(ti.tc.rcvMessage.getHeader()));
        CanMessage expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4C4C);
        expected.setExtended(true);
        Assert.assertEquals(expected, ti.tc.rcvMessage);

        // message for Active and Inactive
        CanMessage mStateActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x194C4123
        );
        mStateActive.setExtended(true);

        CanMessage mStateInactive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 9},
                0x194C4123
        );
        mStateInactive.setExtended(true);

        // check states
        Assert.assertEquals(Sensor.UNKNOWN, t.getKnownState());

        ti.sendMessage(mStateActive);
        Assert.assertEquals(Sensor.ACTIVE,t.getKnownState());

        ti.sendMessage(mStateInactive);
        Assert.assertEquals(Sensor.INACTIVE, t.getKnownState());
    }

    @Test
    public void testMomentarySensor() {
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8", ti.iface);
        s.finishLoad();
        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x195B4123
        );
        mActive.setExtended(true);

        // check states
        Assert.assertEquals(Sensor.UNKNOWN, s.getKnownState());

        ti.sendMessage(mActive);
        Assert.assertEquals(Sensor.ACTIVE,s.getKnownState());

        JUnitUtil.waitFor( ()-> (s.getKnownState() != Sensor.ACTIVE));

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());

        // local flip
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.ACTIVE,s.getKnownState());

        JUnitUtil.waitFor( ()-> (s.getKnownState() != Sensor.ACTIVE));

        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    @Test
    public void testLocalChange() throws jmri.JmriException {
        ti.tc.rcvMessage = null;
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.ACTIVE, t.getKnownState());
        ti.flush();
        Assert.assertNotNull(ti.tc.rcvMessage);
        log.debug("recv msg: {} header {}", ti.tc.rcvMessage, Integer.toHexString(ti.tc.rcvMessage.getHeader()));
        checkActiveMsgSent();
        ti.tc.rcvMessage = null;
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, t.getKnownState());
        ti.flush();
        checkInactiveMsgSent();

        // Repeat send
        ti.tc.rcvMessage = null;
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, t.getKnownState());
        ti.flush();
        checkInactiveMsgSent();
    }

    @Test
    public void testAuthoritative() throws jmri.JmriException {
        t.setState(Sensor.ACTIVE);
        ti.flush();

        // message for Active and Inactive
        CanMessage qActive = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19914123
        );
        qActive.setExtended(true);
        ti.sendMessage(qActive);
        ti.flush();

        CanMessage expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19544c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, ti.tc.rcvMessage);
        ti.tc.rcvMessage = null;

        ((OlcbSensor) t).setAuthoritative(false);
        t.setState(Sensor.INACTIVE);
        ti.flush();

        ti.sendMessage(qActive);
        ti.flush();
        expected = new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                0x19547c4c);
        expected.setExtended(true);
        Assert.assertEquals(expected, ti.tc.rcvMessage);
    }

    @Test
    public void testForgetState() throws JmriException {
        t.dispose(); // dispose of the existing sensor.
        OlcbSensor s = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", ti.iface);
        s.setProperty(OlcbUtils.PROPERTY_LISTEN, Boolean.FALSE.toString());
        s.finishLoad();

        t = s;  // give t a value so the test teardown functions.
        ti.flush();
        ti.tc.rcvMessage = null;

        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");

        s.setKnownState(Sensor.ACTIVE);
        ti.flush();
        Assert.assertEquals(Sensor.ACTIVE, s.getKnownState());
        ti.assertSentMessage(":X195B4c4cN0102030405060708;");

        s.addPropertyChangeListener(l);

        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19544C4CN0102030405060708;");
        // Getting a state notify will not change state now.
        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals("no call", 0, l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Sensor.ACTIVE, s.getKnownState());

        // Resets the turnout to unknown state
        s.setState(Sensor.UNKNOWN);
        JUnitUtil.waitFor( () -> l.getPropertyChanged());
        Assert.assertEquals("called once",1,l.getCallCount());
        l.resetPropertyChanged();
        ti.assertNoSentMessages();

        // state is reported as unknown to the bus
        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19547C4CN0102030405060708;");
        // getting a state notify will change state
        ti.sendMessage(":X19544123N0102030405060709;");
        JUnitUtil.waitFor( () -> l.getPropertyChanged());
        Assert.assertEquals("called once",1,l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());

        // state is reported as known (thrown==invalid)
        ti.sendMessageAndExpectResponse(":X19914123N0102030405060708;",
                ":X19545C4CN0102030405060708;");

        // getting a state notify will not change state
        ti.sendMessage(":X19544123N0102030405060708;");
        Assert.assertEquals("no call", 0, l.getCallCount());
        l.resetPropertyChanged();
        Assert.assertEquals(Sensor.INACTIVE, s.getKnownState());
    }

    @Test
    public void testQueryState() throws Exception {
        OlcbSensor s = (OlcbSensor) t;
        ti.tc.rcvMessage = null;
        Assert.assertEquals(Sensor.UNKNOWN, s.getState());

        // Default sensors listen to identified messages at all times.
        ti.sendMessage(":X19544123N0102030405060708;");
        Assert.assertEquals(Sensor.ACTIVE, s.getState());

        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals(Sensor.INACTIVE, t.getState());

        ti.tc.rcvMessage = null;
        t.requestUpdateFromLayout();
        ti.assertSentMessage(":X198F4C4CN0102030405060708;");
        ti.sendMessage(":X19544123N0102030405060708;");
        Assert.assertEquals(Sensor.ACTIVE, t.getState());

        // Actual sequence from sensor table data model
        t.setKnownState(Sensor.UNKNOWN);
        ti.tc.rcvMessage = null;
        t.requestUpdateFromLayout();
        ti.assertSentMessage(":X198F4C4CN0102030405060708;");
        Assert.assertEquals(Sensor.UNKNOWN, t.getState());
        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals(Sensor.INACTIVE, t.getState());
    }

    @Test
    public void testQueryStateNotAlwaysListen() throws Exception {
        OlcbSensor s = (OlcbSensor) t;
        s.setListeningToStateMessages(false);
        ti.flush();
        ti.tc.rcvMessage = null;
        Assert.assertEquals(Sensor.UNKNOWN, s.getState());

        ti.sendMessage(":X19544123N0102030405060708;");
        Assert.assertEquals(Sensor.ACTIVE, s.getState());

        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals(Sensor.ACTIVE, s.getState()); // no change

        ti.tc.rcvMessage = null;
        t.requestUpdateFromLayout();
        ti.assertSentMessage(":X198F4C4CN0102030405060708;");
        Assert.assertEquals(Sensor.ACTIVE, s.getState()); // still no change
        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals(Sensor.INACTIVE, s.getState()); // now it changes

        // Actual sequence from sensor table data model
        t.setKnownState(Sensor.UNKNOWN);
        ti.tc.rcvMessage = null;
        t.requestUpdateFromLayout();
        ti.assertSentMessage(":X198F4C4CN0102030405060708;");
        Assert.assertEquals(Sensor.UNKNOWN, t.getState());
        ti.sendMessage(":X19544123N0102030405060709;");
        Assert.assertEquals(Sensor.INACTIVE, t.getState());
    }

    /**
     * In this test we simulate the following scenario: A sensor T that is being
     * changed locally by JMRI (e.g. due to a panel icon action), which triggers
     * a Logix, and in that Logix there is an action that sets a second Sensor
     * U. We check that the messages sent to the layout are in the order of
     * T:=Active, U:=Active. There was a multiple-year-long regression that
     * caused these two events to be sent to the network out of order (U first
     * then T).
     */
    @Test
    public void testListenerOutOfOrder() {
        final OlcbSensor u = new OlcbSensor("M", "1.2.3.4.5.6.7.a;1.2.3.4.5.6.7.b", ti.iface);
        final OlcbSensor v = (OlcbSensor)t;
        u.finishLoad();
        v.setKnownState(Sensor.INACTIVE);
        u.setKnownState(Sensor.INACTIVE);

        ti.clearSentMessages();

        v.addPropertyChangeListener("KnownState", propertyChangeEvent -> {
            Assert.assertEquals(Sensor.ACTIVE, t.getKnownState());
            u.setKnownState(Sensor.ACTIVE);
        });

        ThreadingUtil.runOnLayout(() -> v.setKnownState(Sensor.ACTIVE));

        Assert.assertEquals(Sensor.ACTIVE, t.getKnownState());
        Assert.assertEquals(Sensor.ACTIVE, u.getKnownState());

        // Ensures that the last sent message is U==Active. Particularly important that it is NOT
        // the message ending with 0708.
        ti.assertSentMessage(":X195B4C4CN010203040506070A;");
    }

    @Test
    public void testEventTable() {
        EventTable.EventTableEntry[] elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertTrue("Incorrect name: " + elist[0].getDescription(), Pattern.compile("Sensor.*Active").matcher(elist[0].getDescription()).matches());

        t.setUserName("MyInput");

        elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.8")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Sensor MyInput Active", elist[0].getDescription());

        elist = ti.iface.getEventTable()
                .getEventInfo(new EventID("1.2.3.4.5.6.7.9")).getAllEntries();

        Assert.assertEquals(1, elist.length);
        Assert.assertEquals("Sensor MyInput Inactive", elist[0].getDescription());
    }

    @Test
    public void testSystemSpecificComparisonOfSpecificFormats() {

        // test by putting into a tree set, then extracting and checking order
        TreeSet<Sensor> set = new TreeSet<>(new NamedBeanComparator<>());

        set.add(new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", ti.iface));
        set.add(new OlcbSensor("M", "X0501010114FF2000;X0501010114FF2011", ti.iface));
        set.add(new OlcbSensor("M", "X0501010114FF2000;X0501010114FF2001", ti.iface));
        set.add(new OlcbSensor("M", "1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", ti.iface));

        Iterator<Sensor> it = set.iterator();

        Assert.assertEquals("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MS1.2.3.4.5.6.7.9;1.2.3.4.5.6.7.9", it.next().getSystemName());
        Assert.assertEquals("MSX0501010114FF2000;X0501010114FF2001", it.next().getSystemName());
        Assert.assertEquals("MSX0501010114FF2000;X0501010114FF2011", it.next().getSystemName());
    }

    OlcbTestInterface ti;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
       // this test is run separately because it leaves a lot of threads behind
        l = new PropertyChangeListenerScaffold();
        // load dummy TrafficController
        ti = new OlcbTestInterface();
        ti.waitForStartup();
        t = new OlcbSensor("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", ti.iface);
        ((OlcbSensor) t).finishLoad();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning") == false) {
            t.dispose();
            t = null;
            l.resetPropertyChanged();
            l = null;
            ti.dispose();
            ti = null;
        }
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
