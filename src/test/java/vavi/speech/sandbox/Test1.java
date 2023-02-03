/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import vavi.util.Debug;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-23 nsano initial version <br>
 */
class Test1 {

    @Test
    @DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*")
    void test1() throws Exception {
        MacChromeCookie chromeCookie = new MacChromeCookie();
        var cookie = chromeCookie.getCookie(".openai.com");
cookie.forEach((k, v) -> System.err.println(k + "=" + v));
Debug.println(cookie.get("cf_clearance"));
    }
}

/* */
