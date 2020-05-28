package cu.uci.porter.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class IO {

    /**
     * Save input stream to local private file.
     *
     * @param context
     * @param name
     * @param stream  - the input stream
     * @return True if success, false otherwise.
     */
    public static boolean save(Context context, String name, InputStream stream) {
        FileOutputStream file = null;

        if (stream == null) return false;

        try {
            file = context.openFileOutput(name, Context.MODE_PRIVATE);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return save(context, name, stream, file);
    }

    /**
     * Save input stream to a file on external storage like sd card on device
     * memory. By external we mean storage which is not private to the app.
     *
     * @param context
     * @param name
     * @param stream  - the input stream
     * @return True if success, false otherwise.
     */
    public static boolean saveExternal(Context context, String name, InputStream stream) {
        FileOutputStream file = null;

        if (stream == null) return false;

        try {
            File downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            file = new FileOutputStream(new File(downloads, name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return save(context, name, stream, file);
    }

    /**
     * Save input stream to file.
     *
     * @param context
     * @param name
     * @param stream  - the input stream
     * @param file    - the output file stream
     * @return True if success, false otherwise.
     */
    public static boolean save(Context context, String name, InputStream stream, FileOutputStream file) {
        try {
            int l;
            byte[] buffer = new byte[1024 * 32];
            while ((l = stream.read(buffer)) != -1) file.write(buffer, 0, l);

            file.flush();

            stream.close();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Get the full path in form of a URI of the internal app storage.
     * Useful for intents.
     *
     * @param context
     * @param name
     * @return
     */
    public static Uri getPath(Context context, String name) {
        return Uri.fromFile(context.getFileStreamPath(name));
    }

    /**
     * Get the full path in form of a URI of the external storage like sd card
     * or device memory. By external we mean storage which is not private to the
     * app. Useful for intents.
     *
     * @param context
     * @param name
     * @return
     */
    public static Uri getExternalPath(Context context, String name) {
        File downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        return Uri.fromFile(new File(downloads, name));
    }

    /**
     * Read a stream and return a string. Uses UTF-8 encoding.
     *
     * @param is
     * @return
     */
    public static String readStream(InputStream is) {
        return readStream(is, "UTF-8");
    }

    /**
     * Read a stream and return a string.
     *
     * @param is
     * @param charsetName
     * @return
     */
    public static String readStream(InputStream is, String charsetName) {
        if (is == null) return "";

        try {
            Scanner scanner = new Scanner(is, charsetName);
            return scanner.useDelimiter("\\A").next().trim();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    /**
     * @param sourceLocation
     * @param targetLocation
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copyFile(File sourceLocation, File targetLocation)
            throws FileNotFoundException, IOException {
        if (!targetLocation.exists())
            targetLocation.createNewFile();

        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


}