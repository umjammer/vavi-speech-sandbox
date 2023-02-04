[![Release](https://jitpack.io/v/umjammer/vavi-speech-sandbox.svg)](https://jitpack.io/#umjammer/vavi-speech-sandbox)
[![Java CI](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-speech-sandbox

<div style="vertical-align:middle">
<img src="https://user-images.githubusercontent.com/493908/216398725-5ded666c-567d-40a4-a8f1-83acfc8d60b8.png" width="150" />
<span style="font-size:90pt">+</span>
<img src="https://user-images.githubusercontent.com/493908/216399074-bbdd72f8-333b-4125-9e4d-7e44aeeb248e.png" width="150" />
<sub>© <a href="https://openai.com">OpenAI</a> + <a href="https://seiga.nicovideo.jp/seiga/im10788496?ref=pc_watch_description">坂本アヒル</a></sub>
</div>

 * [chatgpt](https://chat.openai.com/)
 * [yakuwarigo modifier](https://github.com/umjammer/vavi-speech/tree/master/src/main/java/vavi/speech/modifier/yakuwarigo)
 * [voicevox](https://voicevox.hiroshiba.jp/) ([library](https://github.com/umjammer/vavi-speech2))

## Status

 * official api ... [works](src/main/java/vavi/speech/sandbox/ChatGPT1.java)
 * reverse engineering api ... [wip](src/main/java/vavi/speech/sandbox/ChatGPT2.java) (cloudflare api has been changed???)

## Install

 * [maven]((https://jitpack.io/#umjammer/vavi-speech-sandbox))
 * vi local.properties
 ```shell
 $ cat local.properties
 openai.key=XXXXXYYYYYZZZZZZ
 chatgpt.model=text-davinci-003
 user.agent=Mozilla/5.0 ...
 ```

## Usage

 * run java w/ jvmargs below
```
--add-opens java.base/java.lang=ALL-UNNAMED
-Dsen.home=/Users/nsano/src/java/sen/src/main/home
-Djna.library.path=target/test-classes
-Djava.util.logging.config.file=src/test/resources/logging.properties
```

## References

 * https://github.com/Barqawiz/IntelliJava ... by web api
 * https://github.com/AcaiSoftware/chatgpt-java
 * https://github.com/PlexPt/chatgpt-java ... 
 * https://github.com/mlkui/chrome-cookie-password-decryption

## TODO

 * [yakuwarigo](https://en.wikipedia.org/wiki/Yakuwarigo) converter rule is too poor, [help it](https://github.com/umjammer/vavi-speech/issues/7)!
