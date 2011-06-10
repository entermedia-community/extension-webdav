package org.openedit.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.entermedia.Asset;
import org.openedit.entermedia.AssetUtilities;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.search.AssetSecurityArchive;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.openedit.ModuleManager;
import com.openedit.page.Page;
import com.openedit.users.User;

public class MediaFactory extends OpenEditResourceFactory
{
	protected AssetUtilities fieldAssetUtilities;
	protected ModuleManager fieldModuleManager;
	protected AssetSecurityArchive  fieldAssetSecurityArchive;
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public AssetUtilities getAssetUtilities()
	{
		return fieldAssetUtilities;
	}

	public void setAssetUtilities(AssetUtilities inAssetUtilities)
	{
		fieldAssetUtilities = inAssetUtilities;
	}

	public OpenEditResource createNew(Page inParentFolder, String inName, InputStream inStream) throws IOException
	{
		OpenEditResource newchild =  super.createNew(inParentFolder, inName, inStream);
		if(inName.startsWith(".") )
		{
			return newchild;
		}
		String parent = inParentFolder.getName();
		if( parent.equals("Links") || parent.equals("Fonts") || inName.equals("Instructions.txt") || inName.equals("Thumbs.db") || inName.endsWith("_tmp"))
		{
			return newchild;
		}
		String catalogid = inParentFolder.getProperty("catalogid");
		MediaArchive archive = getMediaArchive(catalogid);
		Asset asset = getAssetUtilities().createAssetIfNeeded(newchild.getPage().getContentItem(), archive);
		archive.saveAsset(asset, null);
		return newchild;
	}
	public void replaceContent(WebDavFile inWebDavFile, InputStream inStream, Long inArg1)
	{
		super.replaceContent(inWebDavFile, inStream, inArg1);
		Page page = inWebDavFile.getPage();
		String catalogid = page.getProperty("catalogid");
		MediaArchive archive = getMediaArchive(catalogid);
		Asset asset = archive.getAssetBySourcePath(page);
		if( asset != null)
		{
			archive.removeGeneratedImages(asset);
		}

		//Get the folder as well
		asset = archive.getAssetBySourcePath(getPage( page.getParentPath()));
		if( asset != null)
		{
			archive.removeGeneratedImages(asset);
		}
		else
		{
			asset = getAssetUtilities().createAssetIfNeeded(page.getContentItem(), archive);
			archive.saveAsset(asset, null);
		}
	}
	public void delete(OpenEditResource inOpenEditResource)
	{
		Page page = inOpenEditResource.getPage();
		String catalogid = page.getProperty("catalogid");
		MediaArchive archive = getMediaArchive(catalogid);
		getAssetUtilities().deleteAsset(page.getContentItem(), archive);
		getPageManager().removePage(inOpenEditResource.getPage());
		getResourceCache().remove(inOpenEditResource.getPage().getPath());
	}
	
	/*
	 * inURL looks like /media/webdav/photo/xx/yy.jpg
	 * should return /media/browse/photo/data/assets/xx/yy.jpg
	 */
	public String resolvePath(String inURL)
	{
		if( inURL.endsWith("/"))
		{
			inURL = inURL.substring(0,inURL.length() - 1);
		}
		if(getWebApp() != null && inURL.startsWith(getWebApp()))
		{
			inURL = inURL.substring(getWebApp().length());
		}
		
		int webdavindex = inURL.indexOf("/", 1);
		String app = inURL.substring(0, webdavindex);
		
		if( !inURL.startsWith(app + "/webdav" ) )
		{
			return inURL;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(app);
		int catalogindex = inURL.indexOf("/",webdavindex + 1);
		buffer.append("/browse");
		
		if (catalogindex > -1)
		{
			int sourcepathindex = inURL.indexOf("/", catalogindex + 1);
			if (sourcepathindex > -1)
			{
				String catalog = inURL.substring(catalogindex, sourcepathindex);
				buffer.append(catalog);
				buffer.append("/data/originals");
				String path = inURL.substring(sourcepathindex);
				buffer.append(path);
			}
			else
			{
				String catalog = inURL.substring(catalogindex);
				buffer.append(catalog);
				buffer.append("/data/originals");
			}
		}			
		
		return buffer.toString();
	}
	public MediaArchive getMediaArchive(String inCatalogid)
	{
		if (inCatalogid == null)
		{
			return null;
		}
		MediaArchive archive = (MediaArchive) getModuleManager().getBean(inCatalogid, "mediaArchive");
		return archive;
	}
	
	public void moveTo(OpenEditResource inSource, OpenEditResource inTargetFolder, String inNewName)
	{
		String catalogid = inSource.getPage().getProperty("catalogid");
		MediaArchive archive = getMediaArchive(catalogid);
		Asset asset = getAssetUtilities().getAsset(inSource.getPage().getContentItem(), archive);
		if (asset != null)
		{
			getAssetUtilities().moveAsset(asset, inTargetFolder.getPage().getPath() + "/" + inNewName, archive);
		}
		super.moveTo(inSource, inTargetFolder, inNewName);		
	}

	public void sendContent(Page inPage, OutputStream inOut, Range inRange, Map inParams, String inContentType) throws IOException, NotAuthorizedException
	{
		super.sendContent(inPage, inOut, inRange, inParams, inContentType);
		String catalogid = getCatalogId(inPage);
		MediaArchive archive = getMediaArchive(catalogid);
		Asset asset = getAssetUtilities().getAsset(inPage.getContentItem(), archive);
		if (asset != null)
		{
			//Make thumbnails
			getAssetUtilities().readMetadata(asset, inPage.getContentItem(), archive);
			archive.removeGeneratedImages(asset);
		}
		
	}
	protected String getCatalogId(Page inPage)
	{
		return inPage.getProperty("catalogid");
	}

	public AssetSecurityArchive getAssetSecurityArchive()
	{
		return fieldAssetSecurityArchive;
	}

	public void setAssetSecurityArchive(AssetSecurityArchive inAssetSecurityArchive)
	{
		fieldAssetSecurityArchive = inAssetSecurityArchive;
	}
	
	public List getChildrenPaths(String inPath, String inUserName)
	{
		List childpaths = getPageManager().getChildrenPaths(inPath, false);
		ArrayList resources = new ArrayList();
		User user = getUserManager().getUser(inUserName);
		String catalogid = getCatalogId(getPage(inPath));
		MediaArchive archive = getMediaArchive(catalogid);
		for (Iterator iterator = childpaths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			if(path.endsWith("/CVS") || path.endsWith("/.versions"))
			{
				continue;
			}
			Page page = getPage(path);
			if( catalogid != null)
			{
				String sourcePath = archive.getSourcePathForPage(page);
				Map permissions = getAssetSecurityArchive().checkAssetPermissions(user,catalogid,sourcePath);
				Boolean view = (Boolean)permissions.get("canviewasset");
				if ( view == null || view.booleanValue() )
				{
					Resource r = getResource(path);
					resources.add(r);
				}
			}
			else
			{
				Resource r = getResource(path);
				resources.add(r);
				
			}
		}
		return resources;
	}

	
}
