package com.accela.mobile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by jzhong on 6/17/15.
 */
// ShadowLog is used to redirect the android.util.Log calls to System.out
@Config(shadows = {ShadowLog.class})
@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })

/**
 * Base class for PowerMock tests.
 * Important: the classes that derive from this should end with Test (i.e. not Tests) otherwise the
 * gradle task "test" doesn't pick them up.
 */
public abstract class PowerMockTestCaseBase {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void setUp() {
       // ShadowLog.stream = System.out;
        MockitoAnnotations.initMocks(this);
    }
}