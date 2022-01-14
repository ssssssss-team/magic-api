package org.ssssssss.magicapi.core.logging;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.core.CoreConstants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {

	private final static String[] SPACES = {" ", "  ", "    ", "        ", // 1,2,4,8
			// spaces
			"                ", // 16 spaces
			"                                "}; // 32 spaces

	private final static CachingDateFormatter CACHING_DATE_FORMATTER = new CachingDateFormatter("yyyy-MM-dd HH:mm:ss.SSS");

	private final static TargetLengthBasedClassNameAbbreviator ABBREVIATOR = new TargetLengthBasedClassNameAbbreviator(39);

	private StringBuilder buf = new StringBuilder();

	private Formatter() {
	}


	public static Formatter create() {
		return new Formatter();
	}

	private static void leftPad(StringBuilder buf, String s, int desiredLength) {
		int actualLen = 0;
		if (s != null) {
			actualLen = s.length();
		}
		if (actualLen < desiredLength) {
			spacePad(buf, desiredLength - actualLen);
		}
		if (s != null) {
			buf.append(s);
		}
	}

	private static void rightPad(StringBuilder buf, String s, int desiredLength) {
		int actualLen = 0;
		if (s != null) {
			actualLen = s.length();
		}
		if (s != null) {
			buf.append(s);
		}
		if (actualLen < desiredLength) {
			spacePad(buf, desiredLength - actualLen);
		}
	}

	/**
	 * Fast space padding method.
	 */
	private static void spacePad(StringBuilder sbuf, int length) {
		while (length >= 32) {
			sbuf.append(SPACES[5]);
			length -= 32;
		}

		for (int i = 4; i >= 0; i--) {
			if ((length & (1 << i)) != 0) {
				sbuf.append(SPACES[i]);
			}
		}
	}

	public Formatter timestamp(long timestamp) {
		buf.append(CACHING_DATE_FORMATTER.format(timestamp));
		return this;
	}

	public Formatter space() {
		buf.append(SPACES[0]);
		return this;
	}

	public Formatter value(String value) {
		buf.append(value);
		return this;
	}

	public Formatter newline() {
		buf.append("\n");
		return this;
	}

	public Formatter thread(String value) {
		return alignment(value, 15, 15, true, true);
	}

	public Formatter level(String value) {
		return alignment(value, 5, 2147483647, true, true);
	}

	public Formatter loggerName(String value) {
		return alignment(ABBREVIATOR.abbreviate(value), 40, 40, true, false);
	}

	@Override
	public String toString() {
		return buf.toString();
	}

	public Formatter throwable(Throwable throwable) {
		if (throwable != null) {
			this.newline();
			StringWriter sw = new StringWriter(1024);
			PrintWriter writer = new PrintWriter(sw);
			throwable.printStackTrace(writer);
			writer.close();
			buf.append(sw.getBuffer());
			this.newline();
		}
		return this;
	}

	private Formatter alignment(String value, int min, int max, boolean leftTruncate, boolean leftPad) {
		if (value == null) {
			if (0 < min) {
				spacePad(buf, min);
			}
		} else {
			int len = value.length();
			if (len > max) {
				if (leftTruncate) {
					buf.append(value.substring(len - max));
				} else {
					buf.append(value, 0, max);
				}
			} else if (len < min) {
				if (leftPad) {
					leftPad(buf, value, min);
				} else {
					rightPad(buf, value, min);
				}
			} else {
				buf.append(value);
			}
		}
		return this;
	}

	private static class CachingDateFormatter {

		final SimpleDateFormat sdf;
		long lastTimestamp = -1;
		String cachedStr = null;

		public CachingDateFormatter(String pattern) {
			sdf = new SimpleDateFormat(pattern);
		}

		public final String format(long now) {

			// SimpleDateFormat is not thread safe.

			// See also the discussion in http://jira.qos.ch/browse/LBCLASSIC-36
			// DateFormattingThreadedThroughputCalculator and SelectiveDateFormattingRunnable
			// are also noteworthy

			// The now == lastTimestamp guard minimizes synchronization
			synchronized (this) {
				if (now != lastTimestamp) {
					lastTimestamp = now;
					cachedStr = sdf.format(new Date(now));
				}
				return cachedStr;
			}
		}
	}

	private static class TargetLengthBasedClassNameAbbreviator {

		final int targetLength;

		public TargetLengthBasedClassNameAbbreviator(int targetLength) {
			this.targetLength = targetLength;
		}

		public String abbreviate(String fqClassName) {
			StringBuilder buf = new StringBuilder(targetLength);
			if (fqClassName == null) {
				throw new IllegalArgumentException("Class name may not be null");
			}

			int inLen = fqClassName.length();
			if (inLen < targetLength) {
				return fqClassName;
			}

			int[] dotIndexesArray = new int[ClassicConstants.MAX_DOTS];
			// a.b.c contains 2 dots but 2+1 parts.
			// see also http://jira.qos.ch/browse/LBCLASSIC-110
			int[] lengthArray = new int[ClassicConstants.MAX_DOTS + 1];

			int dotCount = computeDotIndexes(fqClassName, dotIndexesArray);

			// System.out.println();
			// System.out.println("Dot count for [" + className + "] is " + dotCount);
			// if there are not dots than abbreviation is not possible
			if (dotCount == 0) {
				return fqClassName;
			}
			// printArray("dotArray: ", dotArray);
			computeLengthArray(fqClassName, dotIndexesArray, lengthArray, dotCount);
			// printArray("lengthArray: ", lengthArray);
			for (int i = 0; i <= dotCount; i++) {
				if (i == 0) {
					buf.append(fqClassName.substring(0, lengthArray[i] - 1));
				} else {
					buf.append(fqClassName.substring(dotIndexesArray[i - 1], dotIndexesArray[i - 1] + lengthArray[i]));
				}
				// System.out.println("i=" + i + ", buf=" + buf);
			}

			return buf.toString();
		}

		int computeDotIndexes(final String className, int[] dotArray) {
			int dotCount = 0;
			int k = 0;
			while (true) {
				// ignore the $ separator in our computations. This is both convenient
				// and sensible.
				k = className.indexOf(CoreConstants.DOT, k);
				if (k != -1 && dotCount < ClassicConstants.MAX_DOTS) {
					dotArray[dotCount] = k;
					dotCount++;
					k++;
				} else {
					break;
				}
			}
			return dotCount;
		}

		void computeLengthArray(final String className, int[] dotArray, int[] lengthArray, int dotCount) {
			int toTrim = className.length() - targetLength;
			// System.out.println("toTrim=" + toTrim);

			// int toTrimAvarage = 0;

			int len;
			for (int i = 0; i < dotCount; i++) {
				int previousDotPosition = -1;
				if (i > 0) {
					previousDotPosition = dotArray[i - 1];
				}
				int available = dotArray[i] - previousDotPosition - 1;
				// System.out.println("i=" + i + ", available = " + available);

				len = (available < 1) ? available : 1;
				// System.out.println("i=" + i + ", toTrim = " + toTrim);

				if (toTrim > 0) {
					len = (available < 1) ? available : 1;
				} else {
					len = available;
				}
				toTrim -= (available - len);
				lengthArray[i] = len + 1;
			}

			int lastDotIndex = dotCount - 1;
			lengthArray[dotCount] = className.length() - dotArray[lastDotIndex];
		}

	}
}
