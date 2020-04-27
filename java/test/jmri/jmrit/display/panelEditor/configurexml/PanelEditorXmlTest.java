package jmri.jmrit.display.panelEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PanelEditorXmlTest.java
 *
 * Test for the PanelEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PanelEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PanelEditorXml constructor",new PanelEditorXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

