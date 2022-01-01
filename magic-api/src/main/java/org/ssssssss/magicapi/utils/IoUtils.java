package org.ssssssss.magicapi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * IO工具包
 *
 * @author mxd
 */
public class IoUtils {

	private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);

	private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(?!\\.)[\\u4e00-\\u9fa5_a-zA-Z0-9.\\-()]+$");

	public static boolean validateFileName(String name) {
		return FILE_NAME_PATTERN.matcher(name).matches();
	}

	public static List<File> files(File file, String suffix) {
		List<File> list = new ArrayList<>();
		if (file.isDirectory()) {
			File[] files = file.listFiles((path) -> path.isDirectory() || path.getName().endsWith(suffix));
			if (files != null) {
				for (int i = files.length - 1; i >= 0; i--) {
					list.addAll(files(files[i], suffix));
				}
			}
		} else if (file.exists()) {
			list.add(file);
		}
		return list;
	}

	public static List<File> dirs(File file) {
		return subDirs(true, file);
	}

	private static List<File> subDirs(boolean isRoot, File file) {
		List<File> list = new ArrayList<>();
		if (file.isDirectory()) {
			File[] files = file.listFiles(File::isDirectory);
			if (files != null) {
				for (int i = files.length - 1; i >= 0; i--) {
					list.addAll(subDirs(false, files[i]));
				}
			}
			if (!isRoot) {
				list.add(file);
			}
		}
		return list;
	}

	public static byte[] bytes(File file) {
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.error("读取文件失败", e);
			return new byte[0];
		}
	}

	public static String string(File file) {
		return new String(bytes(file), StandardCharsets.UTF_8);
	}

	public static byte[] bytes(InputStream inputStream) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int len;
			while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, len);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			logger.error("读取InputStream失败", e);
			return new byte[0];
		}
	}

	public static String string(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			StringBuilder result = new StringBuilder();
			String line;
			boolean flag = false;
			while ((line = reader.readLine()) != null) {
				if (flag) {
					result.append("\r\n");
				}
				result.append(line);
				flag = true;
			}
			return result.toString();
		} catch (IOException e) {
			logger.error("读取InputStream失败", e);
			return "";
		}
	}

	public static boolean write(File file, byte[] bytes) {
		try {
			Files.write(file.toPath(), bytes);
			return true;
		} catch (IOException e) {
			logger.error("写文件失败", e);
			return false;
		}
	}

	public static boolean write(File file, String content) {
		if (content == null) {
			return false;
		}
		return write(file, content.getBytes());
	}

	public static boolean delete(File file) {
		if (file == null) {
			return true;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (int i = files.length - 1; i >= 0; i--) {
					if (!delete(files[i])) {
						return false;
					}
				}
			}
		}
		if (!file.exists()) {
			return true;
		}
		return file.delete();
	}

	public static void closeDataSource(DataSource dataSource) {
		if (dataSource != null) {
			if (dataSource instanceof Closeable) {
				try {
					((Closeable) dataSource).close();
				} catch (Exception e) {
					logger.warn("Close DataSource error", e);
				}
			} else {
				logger.warn("DataSource can not close");
			}
		}
	}

	public static void close(Closeable closeable){
		try {
			if(closeable != null){
				closeable.close();
			}
		} catch (IOException ignored) {
		}
	}
}
