package nz.govt.natlib.ajhr.proc;

import nz.govt.natlib.ajhr.util.MultiThreadsPrint;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

        String msg = String.format("%s [%.2f%s]", srcFile.getAbsolutePath(), lenCurrentRead * 100 / lenTotal, "%");
        String key = MultiThreadsPrint.putUnFinished(msg);
        while (read > -1) {
            digest.update(buffer, 0, read);

            lenCurrentRead += read;

            msg = String.format("%s [%.2f%s]", srcFile.getAbsolutePath(), lenCurrentRead * 100 / lenTotal, "%");
            MultiThreadsPrint.putUnFinished(key, msg);

            read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        this.digestString = Hex.encodeHexString(digest.digest(), true);

        inputStream.close();

        msg = String.format("%s 100%s digest=%s size=%d", "%", srcFile.getAbsolutePath(), this.digestString, (long) lenTotal);
        MultiThreadsPrint.putFinished(key, msg);

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

        String msg = String.format("%s [%.2f%s]", srcFile.getAbsolutePath(), lenCurrentRead * 100 / lenTotal, "%");
        String key = MultiThreadsPrint.putUnFinished(msg);
        while (read > -1) {
            digest.update(buffer, 0, read);
            outputStream.write(buffer, 0, read);

            lenCurrentRead += read;

            msg = String.format("%s [%.2f%s]", srcFile.getAbsolutePath(), lenCurrentRead * 100 / lenTotal, "%");
            MultiThreadsPrint.putUnFinished(key, msg);

            read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        this.digestString = Hex.encodeHexString(digest.digest(), true);

        outputStream.close();
        inputStream.close();

        msg = String.format("%s 100%s digest=%s size=%d", srcFile.getAbsolutePath(), "%", this.digestString, (long) lenTotal);
        MultiThreadsPrint.putFinished(key, msg);

        return true;
    }

    public String getDigestString() {
        return this.digestString;
    }
}
