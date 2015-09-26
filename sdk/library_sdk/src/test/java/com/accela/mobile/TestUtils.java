package com.accela.mobile;

import android.annotation.TargetApi;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by jzhong on 6/17/15.
 */
public class TestUtils {

    @TargetApi(16)
    public static void assertEquals(final JSONObject expected, final JSONObject actual) {
        // JSONObject.equals does not do an order-independent comparison, so let's roll our own  :(
        if (areEqual(expected, actual)) {
            return;
        }
        Assert.failNotEquals("", expected, actual);
    }

    @TargetApi(16)
    public static void assertEquals(final JSONArray expected, final JSONArray actual) {
        // JSONObject.equals does not do an order-independent comparison, so let's roll our own  :(
        if (areEqual(expected, actual)) {
            return;
        }
        Assert.failNotEquals("", expected, actual);
    }

    public static boolean areEqual(final JSONObject expected, final JSONObject actual) {
        // JSONObject.equals does not do an order-independent comparison, so let's roll our own  :(
        if (expected == actual) {
            return true;
        }
        if ((expected == null) || (actual == null)) {
            return false;
        }

        final Iterator<String> expectedKeysIterator = expected.keys();
        final HashSet<String> expectedKeys = new HashSet<String>();
        while (expectedKeysIterator.hasNext()) {
            expectedKeys.add(expectedKeysIterator.next());
        }

        final Iterator<String> actualKeysIterator = actual.keys();
        while (actualKeysIterator.hasNext()) {
            final String key = actualKeysIterator.next();
            if (!areEqual(expected.opt(key), actual.opt(key))) {
                return false;
            }
            expectedKeys.remove(key);
        }
        return expectedKeys.size() == 0;
    }

    public static boolean areEqual(final JSONArray expected, final JSONArray actual) {
        // JSONObject.equals does not do an order-independent comparison, so we need to check values that are JSONObject
        // manually
        if (expected == actual) {
            return true;
        }
        if ((expected == null) || (actual == null)) {
            return false;
        }
        if (expected.length() != actual.length()) {
            return false;
        }

        final int length = expected.length();
        for (int i = 0; i < length; ++i) {
            if (!areEqual(expected.opt(i), actual.opt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean areEqual(final Object expected, final Object actual) {
        if (expected == actual) {
            return true;
        }
        if ((expected == null) || (actual == null)) {
            return false;
        }
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            return areEqual((JSONObject)expected, (JSONObject)actual);
        }
        if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            return areEqual((JSONArray)expected, (JSONArray)actual);
        }
        return expected.equals(actual);
    }

}
