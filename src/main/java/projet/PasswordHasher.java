package projet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing helper based on PBKDF2-HMAC-SHA256 (JDK built-in, no extra dependency).
 *
 * <p>Stored format: {@code pbkdf2_sha256$<iterations>$<base64 salt>$<base64 hash>}.
 * The value fits in the existing {@code administration.password VARCHAR(200)} column,
 * so the database schema is unchanged.</p>
 *
 * <p>Generate a hash for a new administrator from the command line:</p>
 * <pre>java -cp bin projet.PasswordHasher "MyNewPassword"</pre>
 */
public final class PasswordHasher {

    private static final String PREFIX = "pbkdf2_sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private PasswordHasher() {
    }

    public static String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] dk = derive(password, salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(dk);
    }

    /** True if the stored value is in the hashed format produced by {@link #hash(char[])}. */
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX + "$");
    }

    /**
     * Verifies a candidate password against a stored PBKDF2 value.
     * Returns false for malformed or non-hashed values.
     */
    public static boolean verify(char[] candidate, String stored) {
        if (!isHashed(stored)) return false;
        String[] parts = stored.split("\\$");
        if (parts.length != 4) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(candidate, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 is not available in this JVM", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Command-line utility: prints the hash of the password given as first argument. */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java projet.PasswordHasher <password>");
            System.exit(1);
        }
        System.out.println(hash(args[0].toCharArray()));
    }
}
