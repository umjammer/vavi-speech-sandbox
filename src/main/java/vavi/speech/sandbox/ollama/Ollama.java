/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox.ollama;

import java.util.Arrays;
import java.util.logging.Level;
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
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


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

    /** Ollama web api */
    @Property(name = "ollama.url")
    String url = "http://localhost:11434/";

    @Property(name = "ollama.model")
    String model = "llama3.1";

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
Debug.println("model: " + model + "volume: " + volume + ", speed: " + speed);
        // voicevox
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new VoiceVoxSynthesizerMode());

        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        String voiceName = "ずんだもん(ノーマル)";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(volume);
        synthesizer.getSynthesizerProperties().setSpeakingRate(speed);

        // ollma
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(model);
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER, "今から語尾に「なのだ」を使用して喋ってください").build();
        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);
Debug.println(Level.FINER, chatResult.getResponse());

        try (Terminal terminal = TerminalBuilder.terminal()) {
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            while (true) {
                String prompt = lineReader.readLine("あなた    : ");
                if (prompt.equals("quit")) break;
//Debug.println("prompt: " + prompt);
                requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, prompt).build();
                chatResult = ollamaAPI.chat(requestModel);
                terminal.writer().println("ずんだもん: " + chatResult.getResponse());
                synthesizer.speak(chatResult.getResponse(), null);
            }
        } catch (EndOfFileException e) {
Debug.println("bye...");
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
