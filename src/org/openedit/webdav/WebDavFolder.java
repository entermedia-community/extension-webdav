package org.openedit.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.openedit.OpenEditException;
import com.openedit.page.Page;

public class WebDavFolder extends OpenEditResource implements CollectionResource, MakeCollectionableResource,
		PutableResource, CopyableResource, DeletableResource, LockingCollectionResource
{
	private Logger log = LoggerFactory.getLogger(WebDavFolder.class);

	public CollectionResource createCollection(String inName)
	{
		String parentFolder = getPage().getPath();

		if (!parentFolder.endsWith("/"))
		{
			parentFolder = parentFolder + "/";

		}
		inName = parentFolder + inName;
		if (!inName.endsWith("/"))
		{
			inName = inName + "/";
		}
		Page page = getResourceFactory().getPage(inName);

		if (!page.exists())
		{
			log.info("creating collection :" + page);
			CollectionResource folder = getResourceFactory().createCollection(page);
			return folder;
		}
		else
		{
			// throw new RuntimeException("Folder already existed");
			return null;
		}

	}

	public Resource child(String inPath)
	{
		Resource page = getResourceFactory().getResource(getPage().getName() + "/" + inPath);
		return page;
	}

	public List getChildren(String inUser)
	{
		List childpaths = getResourceFactory().getChildrenPaths(getPage().getPath(), inUser);
	
		return childpaths;

	}

	public Resource createNew(String name, InputStream inStream, Long length, String contentType) throws IOException
	{
		OpenEditResource res = getResourceFactory().createNew(getPage(),name,inStream);
		if( fieldCurrentLock != null)
		{
			LockToken copy = copyLock();
			copy.tokenId = res.getPage().getPath();
			res.setCurrentLock(copy);
		}
		return res;
		
	}


	private LockToken copyLock()
	{
		LockToken token =  new LockToken();
		LockToken parent = getCurrentLock();
		token.info = parent.info;
		token.timeout = parent.timeout;
		token.tokenId = parent.tokenId;
		return token;
	}

	public LockToken createAndLock(String inName, LockTimeout inTimeout, LockInfo inLockInfo)
	{
		//Must be a file to create
		OpenEditResource done = null;
		log.info("Create and locked " + getPage() + "/" + inName);
		
		try
		{
			done = (OpenEditResource)getResourceFactory().createTemp(getPage(),inName);
		}
		catch (IOException e)
		{
			throw new OpenEditException(e);
		}
		LockToken token = new LockToken(done.getPage().getPath() , inLockInfo, inTimeout);
		done.setCurrentLock(token);
		return token;
	}

	public LockResult lock(LockTimeout inLockTimeout, LockInfo inLockInfo)
	{
		for (Iterator iterator = getChildren(inLockInfo.owner).iterator(); iterator.hasNext();)
		{
			OpenEditResource child = (OpenEditResource) iterator.next();
			child.lock(inLockTimeout, inLockInfo);
		}
		return 	super.lock( inLockTimeout, inLockInfo);

	}

	public void unlock(String inId)
	{
		if( getCurrentLock() != null)
		{
			log.info("unlocked " + getCurrentLock().info.owner + " for: " + getPage().getPath());
		}
		//Not sure about the user
		for (Iterator iterator = getChildren(null).iterator(); iterator.hasNext();)
		{
			OpenEditResource child = (OpenEditResource) iterator.next();
			child.unlock(inId);
		}
		super.unlock(inId);

	}
}
