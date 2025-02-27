/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox.ollama;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static java.lang.System.getLogger;


/**
 * Ollama.
 *
 * this doesn't work on intellij console. use terminal.app. see readme.md
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-09-07 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class Ollama {

    private static final Logger logger = getLogger(Ollama.class.getName());

    /** Ollama web api */
    @Property(name = "ollama.url")
    String url = "http://localhost:11434/";

    @Property(name = "ollama.model")
    String model = "llama3.1";

    @Property(name = "ollama.initial")
    String initial = "今から語尾に「なのだ」を使用して喋ってください";

    @Property(name = "ollama.timeout")
    int timeout = 30;

    @Property(name = "voicevox.voice")
    String voice;

    @Property(name = "voicevox.speed")
    int speed = 100;

    @Property(name = "voicevox.volume")
    int volume = 5;

    /** */
    OllamaAPI ollamaAPI;

    Ollama() {
        try {
            PropsEntity.Util.bind(this);

            ollamaAPI = new OllamaAPI(url);

            ollamaAPI.setRequestTimeoutSeconds(timeout);
            ollamaAPI.setVerbose(true);

            if (!ollamaAPI.ping()) {
                throw new IllegalStateException("Ollama is not available at " + url);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Ollama is not available at " + url, e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Ollama app = new Ollama();
        app.exec();
    }

    void exec() throws Exception {
logger.log(Level.DEBUG, "model: " + model + ", volume: " + volume + ", speed: " + speed);
        // voicevox
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new VoiceVoxSynthesizerMode());

        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(this.voice)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(volume);
        synthesizer.getSynthesizerProperties().setSpeakingRate(speed);

        // ollma
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(model);
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER, initial).build();
        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);
logger.log(Level.TRACE, chatResult.getResponse());

        try (Terminal terminal = TerminalBuilder.terminal()) {
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            while (true) {
                String prompt = lineReader.readLine("あなた    : ");
                if (prompt.equals("quit")) break;
//logger.log(Level.DEBUG, "prompt: " + prompt);
                requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, prompt).build();
                chatResult = ollamaAPI.chat(requestModel);
                String result = chatResult.getResponse().replaceFirst("<think>.*</think>", "");
                terminal.writer().println("ずんだもん: " + result);
                Arrays.stream(result.split("\n")).forEach(s -> {
                    s = s.trim();
                    if (!s.isEmpty())
                        synthesizer.speak(s, null);
                });
            }
        } catch (EndOfFileException e) {
logger.log(Level.DEBUG, "bye...");
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
