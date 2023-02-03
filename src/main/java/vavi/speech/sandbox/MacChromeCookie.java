/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.sandbox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import vavi.util.Debug;
import vavix.rococoa.keychain.KeychainPasswordStore;


/**
 * MacChromeCookie.
 * <p>
 * currently MacOS only
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-02-03 nsano initial version <br>
 * @see "https://ichiro-kun.com/post/20888/"
 * @see "https://github.com/benjholla/CookieMonster/blob/master/CookieMonster/src/main/java/cmonster/browsers/ChromeBrowser.java"
 */
public class MacChromeCookie {

    private Cipher cipher;

    private static final byte[] salt = "saltysalt".getBytes();
    private static final int length = 16;
    private static final int iterations = 1003;

    /** */
    public MacChromeCookie() {
        try {
            var keychain = new KeychainPasswordStore();
            String password = keychain.getPassword("Chrome Safe Storage", "Chrome");

            var pBkeySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, length * 8);
            var secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            var secretKey = secretKeyFactory.generateSecret(pBkeySpec);
            byte[] key = secretKey.getEncoded();
            assert key.length == length : "key length must be 16: " + key.length;

            this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            var secretKeySpec = new SecretKeySpec(key, "AES");
            byte[] iv = new byte[length];
            Arrays.fill(iv, (byte) ' ');
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String url = "jdbc:sqlite:file:";
    private static final String username = "sa";
    private static final String password = "sa";

    private static final String kEncryptionVersionPrefix = "v10";

    static final String dbPath = System.getProperty("user.home") + "/Library/Application Support/Google/Chrome/Default/Cookies";

    // NOTE: Chrome uses Win32_FILETIME format
    // NOTE: 11644473600 == strftime("%s", "1601-01-01")
    static final String sql = """
                SELECT
                  host_key,
                  path,
                  is_secure,
                  name,
                  value,
                  encrypted_value,
                  ((expires_utc / 1000000) - 11644473600)
                FROM
                  cookies
                WHERE
                  host_key like ?
            """;

    /** */
    Map<String, String> getCookie(String hostKey) throws IOException {

        var cookie = new StringBuilder();

        Path tmp = Files.createTempFile("chrome_cookie", ".db");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tmp.toFile().delete()));
        Files.copy(Paths.get(dbPath), tmp, StandardCopyOption.REPLACE_EXISTING);
        try (var conn = DriverManager.getConnection(url + tmp, username, password);
             var statement = conn.prepareStatement(sql)) {

            statement.setString(1, "%" + hostKey + "%");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String _hostKey = resultSet.getString(1);
                String path = resultSet.getString(2);
                boolean secure = resultSet.getBoolean(3);
                String name = resultSet.getString(4);
                String value = resultSet.getString(5);
                byte[] encryptedValue = resultSet.getBytes(6);
                int exptime = resultSet.getInt(7);

//Debug.println(StringUtil.getDump(encryptedValue));
                if (encryptedValue.length > 3 &&
                        Arrays.equals(encryptedValue, 0, 3, kEncryptionVersionPrefix.getBytes(), 0, 3)) {
                    encryptedValue = Arrays.copyOfRange(encryptedValue, 3, encryptedValue.length);  // Trim prefix "v10"
                    byte[] decrypted = cipher.doFinal(encryptedValue);
                    value = new String(decrypted, StandardCharsets.US_ASCII);
                }

                exptime = Math.max(exptime, 0);
Debug.print(Level.FINE, Stream.of(_hostKey, "TRUE", path, secure, exptime, name, value).map(String::valueOf).collect(Collectors.joining("\t")));
                cookie.append(name).append("=").append(value).append("; ");
            }

            return Arrays.stream(cookie.toString().split("; "))
                    .map(pair -> pair.split("="))
                    .collect(HashMap::new ,
                            (m, i) -> m.put(i[0], i.length > 1 ? i[1] : null),
                            Map::putAll); // TODO optimizeable?
        } catch (IllegalBlockSizeException | BadPaddingException | SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
