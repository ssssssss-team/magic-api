package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.provider.MagicBackupService;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.magicapi.utils.WebUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicFileBackupService implements MagicBackupService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	private static final String suffix = ".json";

	/**
	 * 保存路径
	 */
	private final File backupDirectory;

	public MagicFileBackupService(File backupDirectory) {
		this.backupDirectory = backupDirectory;
		if(!backupDirectory.exists()){
			backupDirectory.mkdirs();
		}
	}

	@Override
	public void doBackup(Backup backup) {
		if (backup.getCreateDate() == 0) {
			backup.setCreateDate(System.currentTimeMillis());
		}
		if (backup.getCreateBy() == null) {
			backup.setCreateBy(WebUtils.currentUserName());
		}
		File directory = new File(backupDirectory, Instant.ofEpochMilli(backup.getCreateDate()).atZone(ZoneOffset.ofHours(8)).toLocalDate().format(FORMATTER));
		if (!directory.exists()) {
			directory.mkdirs();
		}
		IoUtils.write(new File(directory, getFilename(backup)), JsonUtils.toJsonString(backup));
	}

	@Override
	public List<Backup> backupList(long timestamp) {
		File[] fileArray = backupDirectory.listFiles();
		List<Backup> records = new ArrayList<>(FETCH_SIZE);
		if (fileArray != null) {
			List<File> dirs = Stream.of(fileArray).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
			outer:
			for (File dir : dirs) {
				fileArray = dir.listFiles((_dir, name) -> getTimestampFromFilename(name) < timestamp);
				if (fileArray != null) {
					for (File file : fileArray) {
						records.add(JsonUtils.readValue(IoUtils.string(file), Backup.class));
						if (records.size() >= FETCH_SIZE) {
							break outer;
						}
					}
				}
			}
		}
		return records.stream()
				.sorted(Comparator.comparing(Backup::getCreateDate).reversed())
				.collect(Collectors.toList());
	}

	@Override
	public List<Backup> backupById(String id) {
		return backupByFilenameFilter((dir, name) -> name.endsWith(id + suffix))
				.stream()
				.sorted(Comparator.comparing(Backup::getCreateDate).reversed())
				.collect(Collectors.toList());
	}

	@Override
	public Backup backupInfo(String id, long timestamp) {
		File directory = new File(backupDirectory, Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDate().format(FORMATTER));
		if (directory.exists()) {
			File[] files = directory.listFiles((_dir, name) -> name.startsWith("" + timestamp) && name.endsWith(id + suffix));
			if (files != null && files.length > 0) {
				return JsonUtils.readValue(IoUtils.string(files[0]), Backup.class);
			}
		}
		return null;
	}

	@Override
	public List<Backup> backupByTag(String tag) {
		String tagId = MD5Utils.encrypt(tag);
		return backupByFilenameFilter((dir, name) -> name.endsWith(suffix) && name.contains("-" + tagId + "-"));
	}


	@Override
	public long removeBackup(String id) {
		return getFilesByFilenameFilter((dir, name) -> name.endsWith(id + suffix))
				.stream()
				.filter(File::delete)
				.count();
	}

	@Override
	public long removeBackup(List<String> idList) {
		List<String> filenames = idList.stream().map(it -> it + suffix).collect(Collectors.toList());
		return getFilesByFilenameFilter((dir, name) -> filenames.stream().anyMatch(name::endsWith))
				.stream()
				.filter(File::delete)
				.count();
	}

	@Override
	public long removeBackupByTimestamp(long timestamp) {
		long count = getFilesByFilenameFilter((dir, name) -> name.contains("-") && name.endsWith(".json") && name.length() == 32 + 32 + 13 + 2 + 5 && getTimestampFromFilename(name) < timestamp)
				.stream()
				.filter(File::delete)
				.count();
		// 删除空目录
		File[] files = backupDirectory.listFiles(File::isDirectory);
		if(files != null){
			for (File file : files) {
				String[] list = file.list();
				if(list == null || list.length == 0){
					file.delete();
				}
			}
		}
		return count;
	}

	private String getFilename(Backup backup) {
		return String.format("%s-%s-%s.json", backup.getCreateDate(), MD5Utils.encrypt(Objects.toString(backup.getTag(), "")), backup.getId());
	}

	private Long getTimestampFromFilename(String filename) {
		return Long.valueOf(filename.substring(0, 13));
	}

	private List<File> getFilesByFilenameFilter(FilenameFilter filenameFilter) {
		List<File> list = new ArrayList<>();
		File[] dirs = backupDirectory.listFiles();
		if (dirs != null) {
			for (File dir : dirs) {
				if (dir.isDirectory()) {
					File[] files = dir.listFiles(filenameFilter);
					if (files != null) {
						list.addAll(Arrays.asList(files));
					}

				}
			}
		}
		return list;
	}

	private List<Backup> backupByFilenameFilter(FilenameFilter filenameFilter) {
		return getFilesByFilenameFilter(filenameFilter).stream()
				.map(IoUtils::string)
				.map(it -> JsonUtils.readValue(it, Backup.class).small())
				.collect(Collectors.toList());
	}
}
