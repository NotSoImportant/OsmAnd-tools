package net.osmand.server.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import net.osmand.PlatformUtil;

public class GenerateYMLFromAndroidTranslations {

	public static void main(String[] args) throws XmlPullParserException, IOException {
		String path = "../../../android/OsmAnd/res/";
		convertTranslationsToYml(path);
	}

	public static void convertTranslationsToYml(String path)
			throws FileNotFoundException, XmlPullParserException, IOException {
		File fs = new File(path);
		File outDir = new File("yml-translations");
		outDir.mkdir();
		for (File f : fs.listFiles()) {
			File str = new File(f, "strings.xml");
			if (str.exists()) {
				
				FileOutputStream output = new FileOutputStream(new File(outDir, f.getName() + ".yml"));
				parse(str, output);
				output.close();
			}
		}
	}

	private static void parse(File f, OutputStream out) throws XmlPullParserException, IOException {
		XmlPullParser parser = PlatformUtil.newXMLPullParser();
		FileInputStream fis = new FileInputStream(f);
		parser.setInput(getUTF8Reader(fis));
		int tok;
		String key = "";
		StringBuilder vl = new StringBuilder();
		while ((tok = parser.next()) != XmlPullParser.END_DOCUMENT) {
			if (tok == XmlPullParser.START_TAG) {
				String tag = parser.getName();
				if ("string".equals(tag)) {
					key = parser.getAttributeValue("", "name");
				}
			} else if (tok == XmlPullParser.TEXT) {
				vl.append(parser.getText());
			} else if (tok == XmlPullParser.END_TAG) {
				String tag = parser.getName();
				if ("string".equals(tag)) {
					// replace("\"", "\\\"")
					out.write((key + ": \"" + processLine(vl) + "\"\n").getBytes());
				}
				vl.setLength(0);
			}
		}
		fis.close();
	}

	private static String processLine(StringBuilder vl) {
		for (int i = 1; i < vl.length(); i++) {
			if (vl.charAt(i) == '"' && vl.charAt(i - 1) != '\\') {
				vl.insert(i, '\\');
			} else if (vl.charAt(i) == '\'' && vl.charAt(i - 1) == '\\') {
				vl.deleteCharAt(i - 1);
			} else if (vl.charAt(i) == '?' && vl.charAt(i - 1) == '\\') {
				vl.deleteCharAt(i - 1);
			} else if (vl.charAt(i) == 't' && vl.charAt(i - 1) == '\\') {
				vl.deleteCharAt(i);
				vl.deleteCharAt(i - 1);
				vl.insert(i - 1, '\t');
			} else if (vl.charAt(i) == 'n' && vl.charAt(i - 1) == '\\') {
				vl.deleteCharAt(i);
				vl.deleteCharAt(i - 1);
				vl.insert(i - 1, ' ');
			}
		}

		return vl.toString().trim();
	}

	private static Reader getUTF8Reader(InputStream f) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(f);
		assert bis.markSupported();
		bis.mark(3);
		boolean reset = true;
		byte[] t = new byte[3];
		bis.read(t);
		if (t[0] == ((byte) 0xef) && t[1] == ((byte) 0xbb) && t[2] == ((byte) 0xbf)) {
			reset = false;
		}
		if (reset) {
			bis.reset();
		}
		return new InputStreamReader(bis, "UTF-8");
	}
}
