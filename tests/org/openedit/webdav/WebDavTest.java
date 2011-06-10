package org.openedit.webdav;

import org.openedit.entermedia.BaseEnterMediaTest;
import org.openedit.webdav.MediaFactory;

public class WebDavTest extends BaseEnterMediaTest
{
	public void testResolvePath() throws Exception
	{
		MediaFactory factory = new MediaFactory();
		String url = "/media/webdav/photo/xx/yy.jpg";
		String path = factory.resolvePath(url);
		
		assertEquals("/media/browse/photo/data/assets/xx/yy.jpg", path);
		
		url = "/media/webdav";
		path = factory.resolvePath(url);
		assertEquals("/media/browse", path);

	}
}
