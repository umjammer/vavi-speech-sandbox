/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox.chatgpt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import gg.acai.chatgpt.ChatGPT;
import gg.acai.chatgpt.Conversation;
import gg.acai.chatgpt.exception.ParsedExceptionEntry;
import vavi.speech.modifier.yakuwarigo.YakuwarigoModifier;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * ChatGPT2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-23 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class ChatGPT2 {

    @Property(name = "openai.key")
    String apiKey;

    @Property(name = "chatgpt.model")
    String chatGptModel;

    @Property(name = "user.agent")
    String userAgent;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ChatGPT2 app = new ChatGPT2();
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
        var option = new YakuwarigoModifier.ConvertOption();
        option.disableKutenToExclamation = true;
        option.name = "zundamon";
        option.disablePrefix = true;
        option.disableLongNote = true;
        YakuwarigoModifier modifier = new YakuwarigoModifier(option);

        // cookie
        MacChromeCookie chromeCookie = new MacChromeCookie();
        var cookie = chromeCookie.getCookie(".openai.com");
        String cf_clearance = cookie.get("cf_clearance");
Debug.println("cf_clearance: " + cf_clearance);
        String sessionToken = cookie.get("__Secure-next-auth.session-token");
Debug.println("sessionToken: " + sessionToken);

        // chatgpt
        ChatGPT chatGpt = ChatGPT.newBuilder()
                .sessionToken(sessionToken) // required field: get from cookies
                .cfClearance(cf_clearance) // required to bypass Cloudflare: get from cookies
                .userAgent(userAgent) // required to bypass Cloudflare: google 'what is my user agent'
                .addExceptionAttribute(new ParsedExceptionEntry("exception keyword", Exception.class)) // optional: adds an exception attribute
                .connectTimeout(60L) // optional: specify custom connection timeout limit
                .readTimeout(30L) // optional: specify custom read timeout limit
                .writeTimeout(30L) // optional: specify custom write timeout limit
                .build(); // builds the ChatGPT client
        Conversation conversation = chatGpt.createConversation();

        while (true) {
            System.out.print("Q: ");
            String prompt = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (prompt.equals("quit")) break;
            conversation.sendMessageAsync(prompt)
                    .whenComplete((response) -> { // called when the promise is completed with its response
                        System.out.println("A:");
                        String text = response.getMessage();
                        for (String line : text.split("。")) {
                            System.out.println(line);
                            synthesizer.speak(line, null);
                        }
                    });
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
