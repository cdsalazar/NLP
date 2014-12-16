package edu.uchsc.ccp.opendmap;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.uchsc.ccp.testing.TestMain;

public class DMAPTest {

    private final String[] DMAP_BASIC_TESTS = {"data/test/proprietary-test-suites/basic-tests.xml"};
    private final String[] DMAP_PATTERN_RULE_TESTS = {"data/test/proprietary-test-suites/pattern-rule-tests.xml"};
    private final String[] DMAP_PATTERN_TESTS = {"data/test/proprietary-test-suites/pattern-tests.xml"};
    private final String[] DMAP_SLOT_NESTING_TESTS = {"data/test/proprietary-test-suites/slot-nesting-tests.xml"};
    private final String[] DMAP_SLOT_PROPERTY_TESTS ={"data/test/proprietary-test-suites/slot-property-tests.xml"};
    private final String[] DMAP_SLOT_TESTS = {"data/test/proprietary-test-suites/slot-tests.xml"};


    @Before
    public void setUp() throws Exception {
        TestMain.reset();
    }

    @Test
    public void testDMAP_basicTests() {
        assertTrue(TestMain.runTests(DMAP_BASIC_TESTS));
    }

    @Test
    public void testDMAP_patternRuleTests() {
        assertTrue(TestMain.runTests(DMAP_PATTERN_RULE_TESTS));
    }
    
    @Test
    public void testDMAP_patternTests() {
        assertTrue(TestMain.runTests(DMAP_PATTERN_TESTS));
    }
    
    @Test
    public void testDMAP_slotNestingTests() {
        assertTrue(TestMain.runTests(DMAP_SLOT_NESTING_TESTS));
    }

    @Test
    public void testDMAP_slotPropertyTests() {
        assertTrue(TestMain.runTests(DMAP_SLOT_PROPERTY_TESTS));    
    }

    @Test
    public void testDMAP_slotTests() {
        assertTrue(TestMain.runTests(DMAP_SLOT_TESTS));
    }


}
