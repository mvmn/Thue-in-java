package x.mvmn.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class TextFilesHelper {

	public static void save(File file, String content) throws IOException {
		Writer output = null;
		FileWriter fw = new FileWriter(file);
		output = new BufferedWriter(fw);
		output.write(content);
		output.close();
	}

	public static String load(File file) throws Exception {
		StringBuffer result = new StringBuffer();
		FileReader fr = new FileReader(file);
		BufferedReader input = new BufferedReader(fr);
		try {
			String line = null;
			do {
				line = input.readLine();
				if (line != null)
					result.append(line).append("\n");
			} while (line != null);
		} catch (Exception e) {
			throw e;
		} finally {
			input.close();
		}

		return result.toString();
	}

}
