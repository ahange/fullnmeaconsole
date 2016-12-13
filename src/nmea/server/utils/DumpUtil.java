package nmea.server.utils;

import java.io.PrintStream;
import java.util.Arrays;

public class DumpUtil {
	private final static int LINE_LEN = 16;

	public static void displayDualDump(byte[] ba) {
		displayDualDump(ba, null);
	}

	public static void displayDualDump(String str) {
		displayDualDump(str, null);
	}

	public static void displayDualDump(String str, PrintStream ps) {
		displayDualDump(str.getBytes(), ps);
	}

	public static void displayDualDump(byte[] ba, PrintStream ps) {
		PrintStream out = (ps != null ? ps : System.out);
		String[] sa = DumpUtil.dualDump(ba);
		if (sa != null) {
			Arrays.stream(sa).forEach(str -> out.println("\t" + str));
		}
	}

	public static String[] dualDump(String str) {
		byte[] ba = str.getBytes();
		return dualDump(ba);
	}

	/*
	 * Readable + HexASCII code.
	 * @see LINE_LEN member
	 */
	public static String[] dualDump(byte[] ba) {
		String[] result = null;
		int dim = ba.length / LINE_LEN;
		result = new String[dim + 1];
		for (int l = 0; l < (dim + 1); l++) {
			String lineLeft = "";
			String lineRight = "";
			int start = l * LINE_LEN;
			for (int c = start; c < Math.min(start + LINE_LEN, ba.length); c++) {
				lineLeft += (lpad(Integer.toHexString(ba[c] & 0xFF).toUpperCase(), 2, "0") + " ");
				lineRight += (isAsciiPrintable((char) ba[c]) ? (char) ba[c] : ".");
			}
			lineLeft = rpad(lineLeft, 3 * LINE_LEN, " ");
			result[l] = lineLeft + "    " + lineRight;
		}

		return result;
	}

	public static String dumpHexMess(byte[] mess) {
		String line = "";
		for (int i = 0; i < mess.length; i++)
			line += (lpad(Integer.toHexString(mess[i] & 0xFF).toUpperCase(), 2, "0") + " ");
		return line.trim();
	}

	/**
	 * Might not work with some encodings...
	 *
	 * @param ch
	 * @return
	 */
	public static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}

	public static String lpad(String s, int len, String with) {
		while (s.length() < len)
			s = with + s;
		return s;
	}

	public static String rpad(String s, int len, String with) {
		while (s.length() < len)
			s += with;
		return s;
	}
}
