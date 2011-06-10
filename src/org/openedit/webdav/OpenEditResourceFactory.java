package org.openedit.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.openedit.OpenEditException;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.users.UserManager;
import com.openedit.util.FileUtils;
import com.openedit.util.OutputFiller;

public class OpenEditResourceFactory implements ResourceFactory
{
	private static final Log log = LogFactory.getLog(OpenEditResourceFactory.class);
	
	protected PageManager fieldPageManager;
	protected UserManager fieldUserManager;
	protected Map fieldResourceCache;
	protected String fieldWebApp;
	
	public Map getResourceCache()
	{
		if (fieldResourceCache == null)
		{
			fieldResourceCache = new HashMap();

		}

		return fieldResourceCache;
	}

	public void setResourceCache(Map inResourceCache)
	{
		fieldResourceCache = inResourceCache;
	}

	public UserManager getUserManager()
	{
		return fieldUserManager;
	}

	public void setUserManager(UserManager inUserManager)
	{
		fieldUserManager = inUserManager;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	public OpenEditResource getResource(String inPath)
	{
		return getResource(null, inPath);
	}
	public OpenEditResource getResource(String inHost, String inURL)
	{
		String path = resolvePath(inURL);
		
		OpenEditResource resource = (OpenEditResource) getResourceCache().get(path);
		if (resource != null )
		{
			return resource;
		}
		Page page = getPageManager().getPage(path);
		return createResource(page);
	}
	public OpenEditResource getResource(Page page)
	{
		// inHost is not used
		OpenEditResource resource = (OpenEditResource) getResourceCache().get(page.getPath());
		if (resource != null )
		{
			return resource;
		}
		resource = createResource(page);
		return resource;
	}
	
	public String resolvePath(String inURL)
	{
		if(getWebApp() != null && inURL.startsWith(getWebApp()))
		{
			inURL = inURL.substring(getWebApp().length());
		}

		if (inURL.startsWith("/webdav"))
		{
			inURL = inURL.substring("/webdav".length());
		}
		if( inURL.endsWith("/"))
		{
			inURL = inURL.substring(0,inURL.length() - 1);
		}
		return inURL;
	}

	protected OpenEditResource createResource(Page page)
	{
		OpenEditResource resource = null;
		if (page.exists())
		{
			if (page.isFolder())
			{
				WebDavFolder folder = new WebDavFolder();
				folder.setPage(page);
				folder.setResourceFactory(this);
				folder.setUserManager(getUserManager());
				resource = folder;
			}
			else
			{
				WebDavFile file = new WebDavFile();
				file.setPage(page);
				file.setResourceFactory(this);
				file.setUserManager(getUserManager());
				resource = file;
			}
			getResourceCache().put(page.getPath(), resource);
		}
		return resource;
	}

	public String getSupportedLevels()
	{

		return "1,2";
	}

	public void copyTo(OpenEditResource inPage, Page inCopyTo)
	{
		getPageManager().copyPage(inPage.getPage(), inCopyTo);

	}

	public void moveTo(OpenEditResource inSource, OpenEditResource inTargetFolder, String inNewName)
	{
		String dest = inTargetFolder.getPage().getPath() + "/" + inNewName;
		
		Page destination = getPageManager().getPage(dest);
		getPageManager().movePage(inSource.getPage(), destination);
		destination = getPageManager().getPage(dest); //refresh it
		inSource.setPage(destination);
		
		//update the token?
		if( inSource.getCurrentLock() != null)
		{
			inSource.getCurrentLock().tokenId = destination.getPath();
		}
		
		getResourceCache().put(destination.getPath(),inSource);
		
	}

	public void delete(OpenEditResource inOpenEditResource)
	{
		getPageManager().removePage(inOpenEditResource.getPage());
		getResourceCache().remove(inOpenEditResource.getPage().getPath());
	}

	public boolean exists(String inPath)
	{
		return getPageManager().getPage(inPath).exists();
	}

	public Page getPage(String inPath)
	{
		return getPageManager().getPage(inPath);
	}

	public CollectionResource createCollection(Page inPage)
	{
		getPageManager().putPage(inPage);

		return (CollectionResource) getResource(inPage);
	}

	public List getChildrenPaths(String inPath, String inUserName)
	{
		List childpaths = getPageManager().getChildrenPaths(inPath, false);
		ArrayList resources = new ArrayList();
		for (Iterator iterator = childpaths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			if(path.endsWith("/CVS") || path.endsWith("/.versions"))
			{
				continue;
			}
			Resource r = getResource(path);
			resources.add(r);
		}
		return resources;
	}

	public OpenEditResource createTemp(Page inParentFolder, String name) throws IOException
	{
		String filename = inParentFolder.getPath() + "/" + name;
		Page page = getPage(filename);
	
		if (!page.exists())
		{
			log.info("creating new file :" + page);
			getPageManager().putPage(page);
		}
		OpenEditResource res = getResource(filename);
		return res;
		
	}
	public OpenEditResource createNew(Page inParentFolder, String name, InputStream inStream) throws IOException
	{
		String filename = inParentFolder.getPath() + "/" + name;
		Page page = getPage(filename);
	
		if (!page.exists())
		{
			log.info("creating new file :" + page);
			getPageManager().putPage(page); //this might take a moment to create the page?
		}
		if( inStream == null)
		{
			OpenEditResource res = getResource(page.getPath());
			return res;
			
		}
		if( page.isFolder())
		{
			throw new OpenEditException("Trying to save content to a folder " + filename);
		}
		//Now just write it out
		OutputStream out = page.getContentItem().getOutputStream();
		try
		{
			new OutputFiller().fill(inStream, out);
		}
		finally
		{
			FileUtils.safeClose(out);
			FileUtils.safeClose(inStream);
		}
		//If folder is locked then lock the child objects?
		OpenEditResource res = getResource(page.getPath());
		return res;

	}	

	public void replaceContent(WebDavFile inWebDavFile, InputStream inStream, Long inArg1)
	{
		//Now just write it out
		OutputStream out = inWebDavFile.getPage().getContentItem().getOutputStream();
		try
		{
			new OutputFiller().fill(inStream, out);
		}
		catch ( IOException ex)
		{
			throw new OpenEditException(ex);
		}
		finally
		{
			FileUtils.safeClose(out);
			FileUtils.safeClose(inStream);
		}
		
	}

	
	public void sendContent(Page inPage, OutputStream inOut, Range inRange, Map inParams, String inContentType) throws IOException, NotAuthorizedException
	{
		InputStream in = inPage.getInputStream();
		try
		{
			new OutputFiller().fill(in, inOut);
		}
		finally
		{
			FileUtils.safeClose(in);
			FileUtils.safeClose(inOut);
		}
	}

	public String getWebApp()
	{
		return fieldWebApp;
	}

	public void setWebApp(String inWebApp)
	{
		fieldWebApp = inWebApp;
	}


	
	
}
