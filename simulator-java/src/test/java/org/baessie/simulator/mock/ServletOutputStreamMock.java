package org.baessie.simulator.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class ServletOutputStreamMock extends ServletOutputStream {

	private ByteArrayOutputStream baos;
	private String charset;

	public ServletOutputStreamMock() {
		baos = new ByteArrayOutputStream();
	}

	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}

	public String getString() {
		try {
			return baos.toString(charset);
		} catch (Exception e) {
			return baos.toString();
		}
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

}
