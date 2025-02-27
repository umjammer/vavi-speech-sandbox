[![Release](https://jitpack.io/v/umjammer/vavi-speech-sandbox.svg)](https://jitpack.io/#umjammer/vavi-speech-sandbox)
[![Java CI](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-speech-sandbox/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-21-b07219)

# vavi-speech-sandbox

<img alt="ollama" src="https://github.com/user-attachments/assets/b2870d32-1db4-4130-9eef-9fca6023ca40" width="90" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<span style="font-size:4em;"> ✖ </span>
<img src="https://user-images.githubusercontent.com/493908/216399074-bbdd72f8-333b-4125-9e4d-7e44aeeb248e.png" width="120" alt="ずんだもん" /><sub><a href="https://seiga.nicovideo.jp/seiga/im10788496?ref=pc_watch_description"> © 坂本アヒル</a></sub>

 * [Ollama](https://github.com/ollama4j/ollama4j)
 * [VoiceVox](https://voicevox.hiroshiba.jp/) ([library](https://github.com/umjammer/vavi-speech2))

### Status

 * ollama ... works
 * obsoleted
   * chatgpt: official api ... [works](src/main/java/vavi/speech/sandbox/ChatGPT1.java)
   * reverse engineering api ... [wip](src/main/java/vavi/speech/sandbox/ChatGPT2.java) (cloudflare api has been changed???)

## Install

### maven

 * [maven]((https://jitpack.io/#umjammer/vavi-speech-sandbox))

### local.properties

#### voicebox

```shell
voicevox.voice=ずんだもん(ノーマル)
voicevox.volume=10
voicevox.speed=115
```

#### ollama

```shell
ollama.model=llama3.1:latest
ollama.timeout=3000
ollama.initial=今から語尾に「なのだ」を使用して喋ってください。
```

#### chatgpt

```shell
openai.key=XXXXXYYYYYZZZZZZ
chatgpt.model=text-davinci-003
user.agent=Mozilla/5.0 ...
```

## Usage

 * run voicevox.app before running this program
 * run java w/ jvmargs below
```
--add-opens java.base/java.lang=ALL-UNNAMED
-Dsen.home=/Users/nsano/src/java/sen/src/main/home
-Djna.library.path=target/test-classes
-Djava.util.logging.config.file=src/test/resources/logging.properties
```

### ollama
```shell
$ java -Djava.util.logging.config.file=src/test/resources/logging.properties -cp target/vavi-speech-sandbox-0.0.2-SNAPSHOT.jar:$(jpomclasspath) vavi.speech.sandbox.ollama.Ollama
```
[jpomclasspath](https://gist.github.com/umjammer/0a56f0a3a6e7f7ebec641661fd6bb36d) ... a shell script creates the classpath includes all dependency jars in the pom.xml

## References

 * https://github.com/mlkui/chrome-cookie-password-decryption
 * https://github.com/acheong08/EdgeGPT
 * [ChatGPT](https://chat.openai.com/)
   * https://github.com/Barqawiz/IntelliJava ... official api
   * https://github.com/AcaiSoftware/chatgpt-java
   * https://github.com/PlexPt/chatgpt-java ...
 * BingAIChat
   * https://github.com/x28/inscriptis-java
   * https://github.com/vsch/flexmark-java
   * https://github.com/jline/jline3
 * Bard
   * https://github.com/LarryDpk/Google-Bard
 * [Ollama](https://github.com/ollama/ollama)
   * https://qiita.com/s3kzk/items/3cebb8d306fb46cabe9f (japanese model)
   * https://github.com/ollama4j/ollama4j
 * [yakuwarigo modifier](https://github.com/umjammer/vavi-speech/tree/master/src/main/java/vavi/speech/modifier/yakuwarigo)

## TODO

 * [yakuwarigo](https://en.wikipedia.org/wiki/Yakuwarigo) converter rule is too poor, [help it](https://github.com/umjammer/vavi-speech/issues/7)!
   * it's easier to order a chat ai to use a yakuwarigo at beginning 😛
