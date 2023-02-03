package vavi.speech.sandbox;/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import com.intellijava.core.controller.RemoteLanguageModel;
import com.intellijava.core.model.input.LanguageModelInput;
import vavi.speech.modifier.yakuwarigo.YakuwarigoModifier;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * ChatGPT1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-23 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class ChatGPT1 {

    @Property(name = "openai.key")
    String apiKey;

    @Property(name = "chatgpt.model")
    String chatGptModel;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ChatGPT1 app = new ChatGPT1();
        PropsEntity.Util.bind(app);
        app.exec();
    }

    void exec() throws Exception {

        // voicevox
        EngineManager.registerEngineListFactory(vavi.speech.voicevox.jsapi2.VoiceVoxEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode.DEFAULT);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        String voiceName = "ずんだもん(ノーマル)";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(5);

        // yakuwarigo
        YakuwarigoModifier.ConvertOption option = new YakuwarigoModifier.ConvertOption();
        option.disableKutenToExclamation = true;
        option.name = "zundamon";
        option.disablePrefix = true;
        option.disableLongNote = true;
        YakuwarigoModifier modifier = new YakuwarigoModifier(option);

        // 1- initiate the remote language chatGptModel
        RemoteLanguageModel langModel = new RemoteLanguageModel(apiKey, "openai");

        // 2- call generateText with any command !
        while (true) {
            System.out.print("Q: ");
            String prompt = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (prompt.equals("quit")) break;
            LanguageModelInput langInput = new LanguageModelInput.Builder(prompt)
                    .setModel(chatGptModel)
                    .setTemperature(0.5f)
                    .setMaxTokens(1024).build();
            String resValue = langModel.generateText(langInput);
            System.out.println("A:");
            resValue = modifier.convert(resValue);
            for (String line : resValue.split("。")) {
                System.out.println(line);
                synthesizer.speak(line, null);
            }
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
