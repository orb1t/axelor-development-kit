/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

import com.axelor.common.VersionUtils;
import com.google.common.io.Files;

public class UpdateVersion extends DefaultTask {
	
	public static final String TASK_NAME = "updateVersion";
	public static final String TASK_DESCRIPTION = "Update version text in source files.";
	public static final String TASK_GROUP = "Axelor";

	private static final Pattern XML_PATTERN = Pattern.compile("(domain-models|object-views|data-import)_\\d+\\.\\d+\\.xsd");
	private static final Pattern JSON_PATTERN = Pattern.compile("version\".*,");
	private static final Pattern XSD_PATTERN = Pattern.compile("version=\"(\\d+\\.\\d+)\"\\s*\\>");

	private String version = VersionUtils.getVersion().version;
	private String feature = VersionUtils.getVersion().feature;

	@InputFiles
	@SkipWhenEmpty
	private FileTree processFiles;
	
	public FileTree getProcessFiles() {
		return processFiles;
	}

	public void setProcessFiles(FileTree processFiles) {
		this.processFiles = processFiles;
	}

	@TaskAction
	public void update() throws IOException {
		for (File file : processFiles) {
			processFile(file);
		}
	}

	private void processFile(File file) throws IOException {
		final String name = file.getName();
		final String str = Files.toString(file, Charset.forName("UTF-8"));
		
		String txt = str;
		if (name.endsWith(".xsd")) txt = process_xsd(txt);
		if (name.endsWith(".xml")) txt = process_xml(txt);
		if (name.endsWith(".tmpl")) txt = process_xml(txt);
		if (name.endsWith(".json")) txt = process_json(txt);

		if (str == txt) {
			return;
		}

		getLogger().info("Processing {}", file);
		Files.write(txt, file, Charset.forName("UTF-8"));
	}

	private String process_xml(String text) {
		return XML_PATTERN.matcher(text).replaceAll("$1_" + feature + ".xsd");
	}

	private String process_xsd(String text) {
		return XSD_PATTERN.matcher(text).replaceAll("version=\"" + feature + "\">");
	}

	private String process_json(String text) {
		return JSON_PATTERN.matcher(text).replaceAll("\"version\": \"" + version + "\",");
	}
}