package com.earth2me.essentials.textreader;

import net.ess3.api.IEssentials;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.*;


/**
 * <p>BookInput class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class BookInput implements IText {
    private final static HashMap<String, SoftReference<BookInput>> cache = new HashMap<String, SoftReference<BookInput>>();
    private final transient List<String> lines;
    private final transient List<String> chapters;
    private final transient Map<String, Integer> bookmarks;
    private final transient long lastChange;

    /**
     * <p>Constructor for BookInput.</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param createFile a boolean.
     * @param ess a {@link net.ess3.api.IEssentials} object.
     * @throws java.io.IOException if any.
     */
    public BookInput(final String filename, final boolean createFile, final IEssentials ess) throws IOException {

        File file = null;
        if (file == null || !file.exists()) {
            file = new File(ess.getDataFolder(), filename + ".txt");
        }
        if (!file.exists()) {
            if (createFile) {
                final InputStream input = ess.getResource(filename + ".txt");
                final OutputStream output = new FileOutputStream(file);
                try {
                    final byte[] buffer = new byte[1024];
                    int length = input.read(buffer);
                    while (length > 0) {
                        output.write(buffer, 0, length);
                        length = input.read(buffer);
                    }
                } finally {
                    output.close();
                    input.close();
                }
                ess.getLogger().info("File " + filename + ".txt does not exist. Creating one for you.");
            }
        }
        if (!file.exists()) {
            lastChange = 0;
            lines = Collections.emptyList();
            chapters = Collections.emptyList();
            bookmarks = Collections.emptyMap();
            throw new FileNotFoundException("Could not create " + filename + ".txt");
        } else {
            lastChange = file.lastModified();
            boolean readFromfile;
            synchronized (cache) {
                final SoftReference<BookInput> inputRef = cache.get(file.getName());
                BookInput input;
                if (inputRef == null || (input = inputRef.get()) == null || input.lastChange < lastChange) {
                    lines = new ArrayList<String>();
                    chapters = new ArrayList<String>();
                    bookmarks = new HashMap<String, Integer>();
                    cache.put(file.getName(), new SoftReference<BookInput>(this));
                    readFromfile = true;
                } else {
                    lines = Collections.unmodifiableList(input.getLines());
                    chapters = Collections.unmodifiableList(input.getChapters());
                    bookmarks = Collections.unmodifiableMap(input.getBookmarks());
                    readFromfile = false;
                }
            }
            if (readFromfile) {
                final Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
                final BufferedReader bufferedReader = new BufferedReader(reader);
                try {
                    int lineNumber = 0;
                    while (bufferedReader.ready()) {
                        final String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.length() > 0 && line.charAt(0) == '#') {
                            bookmarks.put(line.substring(1).toLowerCase(Locale.ENGLISH).replaceAll("&[0-9a-fk]", ""), lineNumber);
                            chapters.add(line.substring(1).replace('&', '§').replace("§§", "&"));
                        }
                        lines.add(line.replace('&', '§').replace("§§", "&"));
                        lineNumber++;
                    }
                } finally {
                    reader.close();
                    bufferedReader.close();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getLines() {
        return lines;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getChapters() {
        return chapters;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Integer> getBookmarks() {
        return bookmarks;
    }
}
