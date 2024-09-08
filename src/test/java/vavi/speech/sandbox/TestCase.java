/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox;

import org.junit.jupiter.api.Test;
import vavi.speech.sandbox.bingai.BingAIChat;
import vavi.speech.sandbox.ollama.Ollama;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-23 nsano initial version <br>
 */
class TestCase {

    @Test
    void test1() throws Exception {
        BingAIChat.main(new String[] {});
    }

    @Test
    void test2() throws Exception {
        Ollama.main(new String[] {});
    }
}
