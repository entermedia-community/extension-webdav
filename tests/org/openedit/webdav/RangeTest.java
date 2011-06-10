package org.openedit.webdav;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.openedit.webdav.WebDavFile;

import com.bradmcevoy.http.Range;
import com.openedit.BaseTestCase;

public class RangeTest extends BaseTestCase
{
		public void testRange() throws Exception
		{
			WebDavFile file = new WebDavFile();
			File input = new File( getRoot().getParentFile(),"/etc/bigfile.txt");

			//at the start
			Range range = new Range(0,9);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			file.writePartialContent(input, range, out);
			assertEquals(10,out.toByteArray().length);

			//In the middle
			range = new Range(10,19);
			out = new ByteArrayOutputStream();
			file.writePartialContent(input, range, out);
			assertEquals(10,out.toByteArray().length);

			//At the end
			range = new Range(20,100);
			out = new ByteArrayOutputStream();
			file.writePartialContent(input, range, out);
			byte[] restoffile = out.toByteArray();
			assertEquals(81,restoffile.length);

			//At the end
			range = new Range(20,-1);
			out = new ByteArrayOutputStream();
			file.writePartialContent(input, range, out);
			assertEquals(81,out.toByteArray().length);
	
			assertEquals(out.toByteArray()[80],restoffile[80]);

		}
}
