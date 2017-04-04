package codegen;

import com.github.javaparser.ParseException;

public class FileParseFaultResult {
	final private ParseException pe;
	final private String fileName;

	public FileParseFaultResult(final ParseException pe, final String fileName) {
		this.pe = pe;
		this.fileName = fileName;
	}

	public ParseException getParseException() {
		return pe;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public String toString() {
		return fileName + "->" + pe.getMessage();
	}
}
