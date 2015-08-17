package com.accela.mobile;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by jzhong on 6/17/15.
 */
// ShadowLog is used to redirect the android.util.Log calls to System.out
@Config(shadows = {ShadowLog.class})
@RunWith(RobolectricTestRunner.class)

/**
 * Base class for Robolectric tests.
 * Important: the classes that derive from this should end with Test (i.e. not Tests) otherwise the
 * gradle task "test" doesn't pick them up.
 */
public abstract class TestCaseBase {
    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        MockitoAnnotations.initMocks(this);
    }
}