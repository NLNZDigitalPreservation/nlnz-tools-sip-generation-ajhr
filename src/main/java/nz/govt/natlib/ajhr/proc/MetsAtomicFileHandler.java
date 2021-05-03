package nz.govt.natlib.ajhr.proc;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class MetsAtomicFileHandler {
    private static final int STREAM_BUFFER_LENGTH = 1024 * 16;
    private final File srcFile;
    private final File destFile;

    private String digestString;

    public MetsAtomicFileHandler(File srcFile) {
        this.srcFile = srcFile;
        this.destFile = new File(FileUtils.getTempDirectory(), srcFile.getName());
    }

    public MetsAtomicFileHandler(File srcFile, File destFile) {
        this.srcFile = srcFile;
        this.destFile = destFile;
    }

    public boolean md5Digest() throws IOException, NoSuchAlgorithmException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        InputStream inputStream = new BufferedInputStream(new FileInputStream(this.srcFile));

        MessageDigest digest = MessageDigest.getInstance("MD5");

        int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);

        double lenTotal = this.srcFile.length(), lenCurrentRead = 0;
        int lenPreviousMsg = 0;

        System.out.print(srcFile.getAbsoluteFile());
        while (read > -1) {
            digest.update(buffer, 0, read);

            lenCurrentRead += read;

            String backspaceString = "\b".repeat(lenPreviousMsg);
            System.out.print(backspaceString);

            String msg = String.format(" [%.2f%s]", lenCurrentRead * 100 / lenTotal, "%");
            System.out.print(msg);
            lenPreviousMsg = msg.length();

            read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);

            /*
                        try {
                            TimeUnit.MILLISECONDS.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
            */
        }

        this.digestString = Hex.encodeHexString(digest.digest(), true);

        inputStream.close();

        String backspaceString = "\b".repeat(lenPreviousMsg);
        System.out.print(backspaceString);

        String msg = String.format(" 100%s digest=%s size=%d", "%", this.digestString, (long) lenTotal);
        System.out.println(msg);

        return true;
    }

    public boolean md5DigestAndCopy() throws IOException, NoSuchAlgorithmException {
        File destFolder = this.destFile.getParentFile();
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        InputStream inputStream = new BufferedInputStream(new FileInputStream(this.srcFile));
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.destFile));

        MessageDigest digest = MessageDigest.getInstance("MD5");

        int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);

        double lenTotal = this.srcFile.length(), lenCurrentRead = 0;
        int lenPreviousMsg = 0;

        System.out.print(srcFile.getAbsoluteFile());
        while (read > -1) {
            digest.update(buffer, 0, read);
            outputStream.write(buffer, 0, read);

            lenCurrentRead += read;

            String backspaceString = "\b".repeat(lenPreviousMsg);
            System.out.print(backspaceString);

            String msg = String.format(" [%.2f%s]", lenCurrentRead * 100 / lenTotal, "%");
            System.out.print(msg);
            lenPreviousMsg = msg.length();

            read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        this.digestString = Hex.encodeHexString(digest.digest(), true);

        outputStream.close();
        inputStream.close();

        String backspaceString = "\b".repeat(lenPreviousMsg);
        System.out.print(backspaceString);

        String msg = String.format(" 100%s digest=%s size=%d", "%", this.digestString, (long) lenTotal);
        System.out.println(msg);

        return true;
    }

    public String getDigestString() {
        return this.digestString;
    }
}
