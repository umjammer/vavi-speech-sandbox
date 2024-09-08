/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.speech.sandbox.bingai.BingAIChat;
import vavi.speech.sandbox.ollama.Ollama;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-23 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Test
    void test1() throws Exception {
        BingAIChat.main(new String[] {});
    }

    @Test
    @Disabled("doesn't work on jbr terminal")
    void test2() throws Exception {
        Ollama.main(new String[] {});
    }
}
