package bca.process;

import java.io.*;
import java.util.StringTokenizer;

public class Kattio extends PrintWriter {
    private final BufferedReader r;
    private StringTokenizer st;

    // standard input
    public Kattio() { this(System.in, System.out); }
    public Kattio(OutputStream o) {
        super(o);
        r = null;
    }
    public Kattio(InputStream i, OutputStream o) {
        super(o);
        r = new BufferedReader(new InputStreamReader(i));
    }
    public Kattio(String inputFile) throws IOException {
        super(System.out);
//        System.err.println("Reading from file: " + inputFile);
        try {
            r = new BufferedReader(new FileReader(inputFile));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }
    public Kattio(String inputFile, String outputFile) throws IOException {
        super(outputFile != null ? (new BufferedOutputStream(new FileOutputStream(outputFile))) : System.out);
        try {
            r = inputFile != null ? new BufferedReader(new FileReader(inputFile)) : null;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }
    public String next() {
        try {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(r.readLine());
            return st.nextToken();
        } catch (Exception ignored) {}
        return null;
    }

    public int nextInt() { return Integer.parseInt(next()); }
    public double nextDouble() { return Double.parseDouble(next()); }
    public long nextLong() { return Long.parseLong(next()); }
}

