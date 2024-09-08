/*
 * https://github.com/acheong08/EdgeGPT/blob/master/src/EdgeGPT.py
 */

package vavi.speech.sandbox.bingai;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import ch.x28.inscriptis.Inscriptis;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.EOFError;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.utils.InputStreamReader;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.klab.commons.cli.Binder;
import org.klab.commons.cli.Bound;
import org.klab.commons.cli.HelpOption;
import org.klab.commons.cli.Option;
import org.klab.commons.cli.Options;
import vavi.net.auth.cookie.chrome.mac.MacChromeCookie;
import vavi.util.Debug;


/**
 * @see "https://github.com/acheong08/EdgeGPT/blob/master/src/EdgeGPT.py"
 */
@Options
@HelpOption(argName = "help", option = "?", description = "print this help")
public class BingAIChat {

    static String DELIMITER = "0x1e";

    private static Random random = new Random();

    // Generate random IP between range 13.104.0.0/14
    static String FORWARDED_IP = String.format("%d,%d,%d,%d", 13, 104 + random.nextInt(3), random.nextInt(255), random.nextInt(255));

    static Map<String, String> HEADERS = new HashMap<>() {{
        put("accept", "application/json");
        put("accept-language", "en-US,en;q=0.9");
        put("content-type", "application/json");
        put("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Microsoft Edge\";v=\"110\" \"Chromium\";v=\"110\"");
        put("sec-ch-ua-arch", "\"x86\"");
        put("sec-ch-ua-bitness", "\"64\"");
        put("sec-ch-ua-full-version", "\"109.0.1518.78\"");
        put("sec-ch-ua-full-version-list", "\"Chromium\";v=\"110.0.5481.192\", \"Not A(Brand\";v=\"24.0.0.0\", \"Microsoft Edge\";v=\"110.0.1587.69\"");
        put("sec-ch-ua-mobile", "?0");
        put("sec-ch-ua-model", "\"\"");
        put("sec-ch-ua-platform", "\"Windows\"");
        put("sec-ch-ua-platform-version", "\"15.0.0\"");
        put("sec-fetch-dest", "empty");
        put("sec-fetch-mode", "cors");
        put("sec-fetch-site", "same-origin");
        put("x-ms-client-request-id", UUID.randomUUID().toString());
        put("x-ms-useragent", "azsdk-js-api-client-factory/1.0.0-beta.1 core-rest-pipeline/1.10.0 OS/Win32");
        put("Referer", "https://www.bing.com/search?q=Bing+AI&showconv=1&FORM=hpcodx");
        put("Referrer-Policy", "origin-when-cross-origin");
        put("x-forwarded-for", FORWARDED_IP);
    }};

    static Map<String, String> HEADERS_INIT_CONVER = new HashMap<>() {{
        put("authority", "edgeservices.bing.com");
        put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        put("accept-language", "en-US,en;q=0.9");
        put("cache-control", "max-age=0");
        put("sec-ch-ua", "\"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Microsoft Edge\";v=\"110\"");
        put("sec-ch-ua-arch", "\"x86\"");
        put("sec-ch-ua-bitness", "\"64\"");
        put("sec-ch-ua-full-version", "\"110.0.1587.69\"");
        put("sec-ch-ua-full-version-list", "\"Chromium\";v=\"110.0.5481.192\", \"Not A(Brand\";v=\"24.0.0.0\", \"Microsoft Edge\";v=\"110.0.1587.69\"");
        put("sec-ch-ua-mobile", "?0");
        put("sec-ch-ua-model", "\"\"");
        put("sec-ch-ua-platform", "\"Windows\"");
        put("sec-ch-ua-platform-version", "\"15.0.0\"");
        put("sec-fetch-dest", "document");
        put("sec-fetch-mode", "navigate");
        put("sec-fetch-site", "null");
        put("sec-fetch-user", "?1");
        put("upgrade-insecure-requests", "1");
        put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69");
        put("x-edge-shopping-flag", "1");
        put("x-forwarded-for", FORWARDED_IP);
    }};

    SSLContext ssl_context;

//    {
//        try {
//            ssl_context = SSLContext.getDefault();
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init((KeyStore)null);
//            ssl_context.load_verify_locations(certifi.where());
//        } catch (NoSuchAlgorithmException | KeyStoreException e) {
//            throw new IllegalStateException(e);
//        }
//    }

    static class NotAllowedToAccess extends IOException {

        public NotAllowedToAccess(String str) {
            super(str);
        }
    }

    enum ConversationStyle {
        creative("h3imaginative,clgalileo,gencontentv3"),
        balanced("galileo"),
        precise("h3precise,clgalileo");
        final String s;

        ConversationStyle(String s) {
            this.s = s;
        }
    }

    Gson gson = new Gson();

    /**
     * Appends special character to end of message to identify end of message
     */
    String _append_identifier(JsonObject msg) {
        // Convert dict to json string
        return gson.toJson(msg) + DELIMITER;
    }

    /**
     * Returns random hex string
     */
    String _get_ran_hex(int length/* = 32*/) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString();
    }

    /**
     * Request object for ChatHub
     */
    class _ChatHubRequest {

        JsonObject struct;

        String client_id;
        String conversation_id;
        String conversation_signature;
        int invocation_id;

        _ChatHubRequest(
                String conversation_signature,
                String client_id,
                String conversation_id,
                int invocation_id/* = 0*/
        ) {
            this.client_id = client_id;
            this.conversation_id = conversation_id;
            this.conversation_signature = conversation_signature;
            this.invocation_id = invocation_id;
        }

        /**
         * Updates request object
         */
        void update(
                String prompt,
                ConversationStyle conversation_style,
                String... options
        ) {
            if (options == null) {
                options = new String[] {
                        "deepleo",
                        "enable_debug_commands",
                        "disable_emoji_spoken_text",
                        "enablemm",
                };
                if (conversation_style != null) {
                    options = new String[] {
                            "nlu_direct_response_filter",
                            "deepleo",
                            "disable_emoji_spoken_text",
                            "responsible_ai_policy_235",
                            "enablemm",
                            String.valueOf(conversation_style.ordinal()),
                            "dtappid",
                            "cricinfo",
                            "cricinfov2",
                            "dv3sugg",
                    };
                }
                this.struct = gson.fromJson("""
                            "arguments": [
                                {
                                    "source": "cib",
                                    "optionsSets": options,
                                    "sliceIds": [
                                        "222dtappid",
                                        "225cricinfo",
                                        "224locals0",
                                    ],
                                    "traceId": _get_ran_hex(32),
                                    "isStartOfSession": this.invocation_id == 0,
                                    "message": {
                                        "author": "user",
                                        "inputMethod": "Keyboard",
                                        "text": prompt,
                                        "messageType": "Chat",
                                    },
                                    "conversationSignature": this.conversation_signature,
                                    "participant": {
                                        "id": this.client_id,
                                    },
                                    "conversationId": this.conversation_id,
                                },
                            ],
                            "invocationId": String(this.invocation_id),
                            "target": "chat",
                            "type": 4,
                        """, JsonObject.class);
                this.invocation_id += 1;
            }
        }
    }

    /**
     * Conversation API
     */
    class _Conversation {

        JsonObject struct;
        HttpClient session;
        String proxy;

        _Conversation(
                Map<String, String> cookies,
                String proxy
        ) throws IOException, InterruptedException {
            this.struct = gson.fromJson("""
                        {
                        "conversationId": null,
                        "clientId": null,
                        "conversationSignature": null,
                        "result": {"value": "Success", "message": null}
                        }
                    """, JsonObject.class);
            this.proxy = proxy;
            proxy = proxy != null ? proxy :
                    System.getenv("all_proxy") != null ? System.getenv("all_proxy") :
                    System.getenv("ALL_PROXY") != null ? System.getenv("ALL_PROXY") :
                    System.getenv("https_proxy") != null ? System.getenv("https_proxy") :
                    System.getenv("HTTPS_PROXY") != null ? System.getenv("HTTPS_PROXY") :
                    null;
            if (proxy != null && proxy.startsWith("socks5h://")) {
                proxy = "socks5://" + proxy.substring("socks5h://".length());
            }
//            URI uri = URI.create(proxy);
            this.session = HttpClient.newBuilder()
//                    .proxy(ProxySelector.of(InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort())))
                    .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE))
                    .connectTimeout(Duration.of(900, ChronoUnit.SECONDS))
                    .build();
//            /*headers=*/HEADERS_INIT_CONVER
            CookieStore cookieStore = ((CookieManager) session.cookieHandler().get()).getCookieStore();
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                cookieStore.add(URI.create(".bing.com"), new HttpCookie(cookie.getKey(), cookie.getValue()));
            }

            // Send GET request
            String url = System.getenv("BING_PROXY_URL");
            url = url == null ? "https://edgeservices.bing.com/edgesvc/turing/conversation/create" : url;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .headers(HEADERS_INIT_CONVER.entrySet().stream()
                            .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                            .toList()
                            .toArray(String[]::new))
                    .build();
            HttpResponse<String> response = session.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                request = HttpRequest.newBuilder(URI.create("https://edge.churchless.tech/edgesvc/turing/conversation/create"))
                        .GET()
                        .build();
                this.session.send(request, HttpResponse.BodyHandlers.discarding());
            }
            if (response.statusCode() != 200) {
                System.err.print("Status code: {response.status_code}");
                System.err.print(response.body());
                System.err.print(response.uri());
                throw new IllegalStateException("Authentication failed");
            }
            try {
                this.struct = gson.fromJson(response.body(), JsonObject.class);
            } catch (Exception exc) {
                throw new IllegalStateException(
                        "Authentication failed. You have not been accepted into the beta.",
                        exc);
            }
            if (this.struct.get("result").getAsJsonObject().get("value").getAsString().equals("UnauthorizedRequest")) {
                throw new NotAllowedToAccess(this.struct.get("result").getAsJsonObject().get("message").getAsString());
            }
        }
    }

    // https://stackoverflow.com/a/31130128
    public static class MyConfigurator extends ClientEndpointConfig.Configurator {
        @Override public void beforeRequest(Map<String, List<String>> headers) {
            HEADERS.forEach((k, v) -> headers.put(k, Collections.singletonList(v)));
        }
    }

    @ClientEndpoint(configurator = MyConfigurator.class)
    public static class MyEndpointObject {
        Consumer<Session> onOpen;
        BiConsumer<String, Session> onMessage;
        public MyEndpointObject(Consumer<Session> onOpen, BiConsumer<String, Session> onMessage) {
            this.onOpen = onOpen;
            this.onMessage = onMessage;
        }

        @OnOpen public void onOpen(Session session) throws IOException {
Debug.println("WEBSOCKET: onOpen: " + session.getId());
            onOpen.accept(session);
        }

        @OnMessage public void onNotifyMessage(String notification, Session session) throws IOException {
Debug.println("WEBSOCKET: onMessage: " + notification);
        }

        @OnClose public void onClose(Session session) throws IOException {
Debug.println("WEBSOCKET: onClose: " + session.getId());
            session.close();
        }

        @OnError public void onError(Throwable t) {
Debug.println("WEBSOCKET: onError");
            t.printStackTrace();
        }
    }

    /**
     * Chat API
     */
    class _ChatHub {

        Session wss;
        _ChatHubRequest request;
        boolean loop;
        Runnable task;

        _ChatHub(_Conversation conversation) {
            this.wss = null;
            this.request = new _ChatHubRequest(
                    /*conversation_signature=*/conversation.struct.get("conversationSignature").getAsString(),
                    /*client_id=*/conversation.struct.get("clientId").getAsString(),
                    /*conversation_id=*/conversation.struct.get("conversationId").getAsString(),
                    0
            );
        }

        /**
         * Ask a question to the bot
         */
        void/*async*/ ask_stream(
                BiConsumer<Boolean, String> f,
                String prompt,
                String wss_link,
                ConversationStyle conversation_style/* = null*/,
                boolean raw/* = false*/,
                String... options/* = null*/
        ) throws IOException, DeploymentException {
            if (this.wss != null && this.wss.isOpen()) {
                /*await*/
                this.wss.close();
            }
            // Check if websocket is closed
            this.wss = /*await*/ ContainerProvider.getWebSocketContainer().connectToServer(
                    new MyEndpointObject(session -> {
                        // Send request
                        session.getAsyncRemote().sendText(_append_identifier(gson.fromJson("{\"protocol\": \"json\", \"version\": 1}", JsonObject.class)));
                        // Construct a ChatHub request
                        request.update(
                                /*prompt=*/prompt,
                                /*conversation_style=*/conversation_style,
                                /*options=*/options
                        );
                    }, (notification, session) -> {
                        boolean final_ = false;
                        while (!final_) {
                            String[] objects = notification.split(DELIMITER);
                            for (String obj : objects) {
                                if (obj == null || obj.isEmpty()) {
                                    continue;
                                }
                                JsonObject response = gson.fromJson(obj, JsonObject.class);
                                if (response.get("type").getAsInt() != 2 && raw) {
                                    f.accept(false, gson.toJson(response));
                                } else if (response.get("type").getAsInt() == 1 && response.get("arguments").getAsJsonArray().get(0).getAsJsonObject().get("messages") != null) {
                                    String resp_txt = response
                                            .get("arguments").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("messages").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("adaptiveCards").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("body").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("text").getAsString();
                                    f.accept(false, resp_txt);
                                } else if (response.get("type").getAsInt() == 2) {
                                    final_ = true;
                                    f.accept(true, gson.toJson(response));
                                }
                            }
                        }
                    }),
                    URI.create(wss_link)
//                    /*extra_headers=*/HEADERS, // TODO
//                    /*max_size=*/null,
//                    /*ssl=*/ssl_context
            );
            /*await*/
            this.wss.getAsyncRemote().sendText(_append_identifier(this.request.struct));
        }
    }

    /**
     * Combines everything to make it seamless
     */
    class Chatbot {

        String proxy;
        _ChatHub chat_hub;
        Map<String, String> cookies;

        Chatbot(Map<String, String> cookies/* = null*/,
                String proxy/* = null*/,
                String cookie_path/* = null*/
        ) throws IOException, InterruptedException {
            if (cookies == null) {
                cookies = new HashMap<>();
            }
            if (cookie_path != null) {
                try (BufferedReader f = Files.newBufferedReader(Path.of(cookie_path), StandardCharsets.UTF_8)) {
                    this.cookies = gson.fromJson(f, HashMap.class);
                } catch (FileNotFoundException exc) {
                    throw new IOException("Cookie file not found", exc);
                }
            } else {
                this.cookies = cookies;
            }
            this.proxy = proxy;
            this.chat_hub = new _ChatHub(new _Conversation(this.cookies, this.proxy));
        }

        /**
         * Ask a question to the bot
         */
        void /*async*/ ask(
                Consumer<String> f,
                String prompt,
                String wss_link/* = "wss://sydney.bing.com/sydney/ChatHub"*/,
                ConversationStyle conversation_style/* = null*/,
                String... options/* = null*/
        ) throws IOException, DeploymentException {
            /*async*/
            this.chat_hub.ask_stream((final_, response) -> {
                        if (final_) {
                            f.accept(response);
                        } else {
                            try {
                                /*await*/
                                this.chat_hub.wss.close();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    },
                    prompt,
                    wss_link,
                    conversation_style,
                    false,
                    options
            );
        }

        /**
         * Ask a question to the bot
         */
        void /*async*/ ask_stream(
                BiConsumer<Boolean, String> f,
                String prompt,
                String wss_link /*= "wss://sydney.bing.com/sydney/ChatHub"*/,
                ConversationStyle conversation_style/* = null*/,
                boolean raw/* = false*/,
                String... options/* = null*/
        ) throws IOException, DeploymentException {
            /*async*/
            this.chat_hub.ask_stream(
                    (false_, response) -> f.accept(false, response),
                    prompt,
                    wss_link,
                    conversation_style,
                    raw,
                    options
            );
        }

        /**
         * Close the connection
         */
        /*async*/ void close() throws IOException {
            /*await*/
            this.chat_hub.wss.close();
        }

        /**
         * Reset the conversation
         */
        /* async */ void reset() throws IOException, InterruptedException {
            /* await */
            this.close();
            this.chat_hub = new _ChatHub(new _Conversation(this.cookies, null));
        }
    }

    /**
     * Multiline input function.
     */
    /* async */ String _get_input_async(
            LineReader session /*= null */
    ) {
        // TODO multiline https://github.com/jline/jline3/issues/36#issuecomment-652522724
        return /* await */ session.readLine();
    }

    static class MyParser extends DefaultParser {

        @Override
        public ParsedLine parse(String line, int cursor, ParseContext context) {
            if (context == ParseContext.ACCEPT_LINE) {
                if (!line.trim().startsWith("!")) {
                    throw new EOFError(-1, -1, "not end of statement");
                } else {
                    line += "\n";
                }
            } else if (context == ParseContext.COMPLETE) {
                // esc -> cancel
            }
            return super.parse(line, cursor, context);
        }
    }

    LineReader _create_session(Completer completer /* = null */) {

        return LineReaderBuilder.builder()
                .completer(completer)
                .history(new DefaultHistory())
//                .parser(new MyParser()) // TODO
                .build();
//        Reader(/*key_bindings=*/kb);
    }

    static Completer _create_completer(List<String> commands, String pattern_str /* = "$" */) {
        return new Completers.RegexCompleter(pattern_str, s -> new StringsCompleter(commands));
    }

    /**
     * Main function
     */
    /*async*/ void async_main() {
        try {
            System.err.println("Initializing...");
            System.err.println("Enter `alt+enter` or `escape+enter` to send a message");
            Chatbot bot = new Chatbot(this.cookies, /*proxy=*/this.proxy, /*cookies=*/ null);
            Completer completer = _create_completer(Arrays.asList("!help", "!exit", "!reset"), "$");
            LineReader session = _create_session(completer);
            String initial_prompt = this.prompt;
            String question;

            while (true) {
                System.err.println("\nYou:");
                if (initial_prompt != null) {
                    question = initial_prompt;
                    System.err.print(question);
                    initial_prompt = null;
                } else {
                    question = (
                            this.enter_once ? new BufferedReader(new InputStreamReader(System.in)).readLine()
                                    :/* await*/ _get_input_async(/*session=*/session)
                    );
                }
                System.err.println();
                if (question.equals("!exit")) {
                    break;
                }
                if (question.equals("!help")) {
                    System.err.print(
                            """
                                    !help - Show this help message
                                    !exit - Exit the program
                                    !reset - Reset the conversation
                                    """
                    );
                    continue;
                }
                if (question.equals("!reset")) {
                    /*await*/
                    bot.reset();
                    continue;
                }
                System.err.print("Bot:");
                if (this.no_stream) {
                    /*await*/ bot.ask(s -> System.err.print(
                            gson.fromJson(s, JsonObject.class).get("item").getAsJsonObject()
                                    .get("messages").getAsJsonArray().get(1).getAsJsonObject()
                                    .get("adaptiveCards").getAsJsonArray().get(0).getAsJsonObject()
                                    .get("body").getAsJsonArray().get(0).getAsJsonObject()
                                    .get("text").getAsString()
                            ),
                            /*prompt=*/question,
                            /*wss_link=*/this.wss_link,
                            /*conversation_style=*/this.style);
                } else {
                    AtomicInteger wrote = new AtomicInteger();
                    if (this.rich) {
                        Parser parser = Parser.builder().build();
                        HtmlRenderer renderer = HtmlRenderer.builder().build();
                        /*async*/
                        bot.ask_stream((final_, response) -> {
                                    if (!final_) {
                                        if (wrote.get() > response.length()) {
                                            Document md = parser.parse(response);
                                            String html = renderer.render(md);
                                            Inscriptis inscriptis = new Inscriptis(W3CDom.convert(Jsoup.parse(html)));
                                            String text = inscriptis.getText();
                                            System.err.print(text);
                                            System.err.print(parser.parse("***Bing revoked the response.***"));
                                        }
                                        wrote.set(response.length());
                                        Document md = parser.parse(response);
                                        String html = renderer.render(md);
                                        Inscriptis inscriptis = new Inscriptis(W3CDom.convert(Jsoup.parse(html)));
                                        String text = inscriptis.getText();
                                        System.out.println(text);
                                    } else {
                                        try {
                                            /*await*/ bot.close();
                                        } catch (IOException e) {
                                            throw new org.jsoup.UncheckedIOException(e);
                                        }
                                    }
                                },
                                /*prompt=*/question,
                                /*wss_link=*/this.wss_link,
                                /*conversation_style=*/this.style,
                                false
                        );
                    } else {
                        /*async*/
                        bot.ask_stream((final_, response) -> {
                                    if (!final_) {
                                        System.err.print(response.substring(wrote.get()));
                                        System.err.flush();
                                        wrote.set(response.length());
                                    } else {
                                        try {
                                            /*await*/ bot.close();
                                        } catch (IOException e) {
                                            throw new org.jsoup.UncheckedIOException(e);
                                        }
                                    }
                                },
                                /*prompt=*/question,
                                /*wss_link=*/this.wss_link,
                                /*conversation_style=*/this.style,
                                false
                        );
                        System.err.println();
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Option(argName = "enter once", option = "enter_once")
    boolean enter_once;
    @Option(argName = "no stream", option = "no_stream")
    boolean no_stream;
    @Option(argName = "rich", option = "rich")
    boolean rich;
    @Option(argName = "proxy", option = "proxy", args = 1, description = "Proxy URL (e.g. socks5://127.0.0.1:1080)")
    String proxy;
    @Option(argName = "wss link", option = "wss_link", args = 1, description = "WSS URL(e.g. wss://sydney.bing.com/sydney/ChatHub)")
    String wss_link = "wss://sydney.bing.com/sydney/ChatHub";
    @Option(argName = "style", option = "style", args = 1)
    ConversationStyle style = ConversationStyle.balanced;
    @Option(argName = "cookie file", option = "cookie_file", args = 1, description = "Cookie file used for authentication (defaults to COOKIE_FILE environment variable)")
    @Bound(binder = CookieFileBinder.class)
    String cookie_file = System.getenv("COOKIE_FILE");
    @Option(argName = "prompt", option = "prompt", description = "prompt to start with")
    String prompt;

    /** */
    private Map<String, String> cookies;

    static class CookieFileBinder implements Binder<String> {

        @Override
        public void bind(String s, String[] strings, Context context) {
            if (s == null) {
                context.printHelp();
                System.err.println("ERROR: use --cookie-file or set the COOKIE_FILE environment variable");
                System.exit(1);
            }
        }
    }

    /**
     * @param args @Options
     */
    public static void main(String[] args) throws Exception {
        System.err.print("""
                    EdgeGPT - A demo of reverse engineering the Bing GPT chatbot
                    Repo: github.com/acheong08/EdgeGPT
                    By: Antonio Cheong

                    !help for help

                    Type !exit to exit
                """);
        BingAIChat app = new BingAIChat();
        Options.Util.bind(args, app);
        try {
//            app.cookies = app.gson.fromJson(Files.newBufferedReader(Path.of(app.cookie_file)), HashMap.class);
            app.cookies = new MacChromeCookie().getCookie(".bing.com");
//app.cookies.forEach((k, v) -> System.err.println(k + ": " + v));
        } catch (IOException exc) {
            throw new IllegalStateException("Could not open cookie file: " + exc);
        }

//        CompletableFuture.runAsync(app::async_main);
        app.async_main();
    }
}