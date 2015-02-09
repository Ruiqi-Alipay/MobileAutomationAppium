package com.alipay.autotest.mobile.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

public class GZipUtils {
	/** Zip the contents of the directory, and save it in the zipfile */
	public static void zipDirectory(File d, File zipfile)
			throws IOException, IllegalArgumentException {
		// Check that the directory is a directory, and get its contents
		if (!d.isDirectory())
			throw new IllegalArgumentException("Compress: not a directory:  "
					+ d.getAbsolutePath());
		String[] entries = d.list();
		byte[] buffer = new byte[4096]; // Create a buffer for copying
		int bytes_read;

		// Create a stream to compress data and write it to the zipfile
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));

		// Loop through all entries in the directory
		for (int i = 0; i < entries.length; i++) {
			File f = new File(d, entries[i]);
			if (f.isDirectory())
				continue; // Don't zip sub-directories
			FileInputStream in = new FileInputStream(f); // Stream to read file
			ZipEntry entry = new ZipEntry(f.getPath()); // Make a ZipEntry
			out.putNextEntry(entry); // Store entry
			while ((bytes_read = in.read(buffer)) != -1)
				// Copy bytes
				out.write(buffer, 0, bytes_read);
			in.close(); // Close input stream
		}
		// When we're done with the whole loop, close the output stream
		out.close();
	}

	public static void zipDirectory(File source, File destination, String name)
			throws IOException, IllegalArgumentException {
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP,
				CompressionType.GZIP);
		archiver.create(name, destination, source);
	}

	public static void unZip(File source, File destination) throws IOException,
			IllegalArgumentException {
		Archiver archiver = ArchiverFactory.createArchiver("zip");
		archiver.extract(source, destination);
	}
}
