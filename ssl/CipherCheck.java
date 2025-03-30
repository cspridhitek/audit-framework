import javax.net.ssl.SSLServerSocketFactory;
import java.util.Arrays;

public class CipherCheck {
    public static void main(String[] args) {
        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        System.out.println("Supported Ciphers: " + Arrays.toString(factory.getSupportedCipherSuites()));
    }
}