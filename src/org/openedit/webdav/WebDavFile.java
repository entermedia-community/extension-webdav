package org.openedit.webdav;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class WebDavFile extends OpenEditResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource, ReplaceableResource
{
	public Long getContentLength()
	{
		return new Long(getPage().getContentItem().getLength());
	}
	
	public String getContentType(String preferredList)
	{
		return getPage().getMimeType();
	}

	public void sendContent(OutputStream inOut, Range inRange, Map inParams, String inContentType) throws IOException, NotAuthorizedException
	{
		getResourceFactory().sendContent(getPage(), inOut, inRange, inParams, inContentType);
	}

	public void writePartialContent(File inFile, Range range, OutputStream inOut) throws FileNotFoundException, IOException
	{
		RandomAccessFile in = new RandomAccessFile(inFile,"r");
		try
		{
		in.seek(range.getStart());
		
		byte[] bytes = new byte[1024];

		//log.info("Sending file " + getPage().getPath() + " " + range.getStart() + " to " + range.getFinish());
		int iRead = -1;
		long totalSent = range.getStart();
		
			while (true)
			{
				iRead = in.read(bytes);

				if (iRead != -1)
				{
					//Check that we are not close to the end of the finish range
					if( range.getFinish() > -1 && totalSent + iRead - 1 > range.getFinish())
					{
						//limit the output
						//http://tools.ietf.org/html/rfc2616#page-138
						iRead =(int)(range.getFinish() + 1L - totalSent);
		
						inOut.write(bytes, 0, iRead);
						break;
					}
					
					inOut.write(bytes, 0, iRead);
					totalSent = totalSent + iRead;
				}
				else
				{
					break;
				}
			}
			inOut.flush();
		}
		finally
		{
			in.close();
		}
	}

	public Long getMaxAgeSeconds(Auth inAuth)
	{
		return new Long(getPage().getContentItem().getLastModified());//not sure about this one.  Need to look it up
	}
	
	public void replaceContent(InputStream inArg0, Long inArg1)
	{
		// TODO Auto-generated method stub
		getResourceFactory().replaceContent(this,inArg0, inArg1);
	}

}
