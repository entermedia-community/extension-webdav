package org.openedit.webdav;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.LockResult.FailureReason;
import com.bradmcevoy.http.PropPatchHandler.Field;
import com.bradmcevoy.http.PropPatchHandler.Fields;
import com.bradmcevoy.http.PropPatchHandler.SetField;
import com.bradmcevoy.http.Request.Method;
import com.openedit.OpenEditRuntimeException;
import com.openedit.page.Page;
import com.openedit.users.User;
import com.openedit.users.UserManager;
import com.openedit.users.authenticate.AuthenticationRequest;

public abstract class OpenEditResource implements Resource, MoveableResource, CopyableResource,
		LockableResource, PropFindableResource, DeletableResource, PropPatchableResource
{

	protected Page fieldPage;
	protected UserManager fieldUserManager;
	private Logger log = LoggerFactory.getLogger(OpenEditResource.class);
	protected LockToken fieldCurrentLock;
	protected OpenEditResourceFactory fieldResourceFactory;
	
	public LockToken getCurrentLock()
	{
		return fieldCurrentLock;
	}

	public void setCurrentLock(LockToken inLockToken)
	{
		fieldCurrentLock = inLockToken;
	}

	public UserManager getUserManager()
	{
		return fieldUserManager;
	}

	public void setUserManager(UserManager inUserManager)
	{
		fieldUserManager = inUserManager;
	}

	public Page getPage()
	{
		return fieldPage;
	}

	public void setPage(Page inPage)
	{
		fieldPage = inPage;
	}


	public Object authenticate(String inUsername, String inPassword)
	{
		User user = getUserManager().getUser(inUsername);
		if (user == null)
		{
			return null;
		}
		AuthenticationRequest request = new AuthenticationRequest();
		request.setUser(user);
		request.setPassword(inPassword);
		boolean authenticated = getUserManager().getAuthenticator().authenticate(request);
		if(authenticated){
			return user;
		} else{
			return null;
		}
	}

	public boolean authorise(Request request, Method method, Auth auth)
	{
//		String path = request.getAbsolutePath();
		
		
		
		return auth != null;
	}

	public String checkRedirect(Request inArg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Date getModifiedDate()
	{
		return getPage().getLastModified();
	}

	public String getName()
	{
		return getPage().getName();
	}

	public String getRealm()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getUniqueId()
	{
		return getPage().getPath();
	}

	public void copyTo(CollectionResource newParent, String newName) 
	{
		if (newParent instanceof OpenEditResource)
		{
			OpenEditResource targetFolder = (OpenEditResource) newParent;
			Page to = getResourceFactory().getPage(targetFolder.getPage().getPath() + "/" + newName);
			getResourceFactory().copyTo(this, to);
//			if( getLockToken() != null)
//			{
//				//pass along the token?
//				OpenEditResource dest = getResourceFactory().getResource(to.getPath());
//				LockToken token =  new LockToken();
//				token.info = getLockToken().info;
//				token.timeout = getLockToken().timeout;
//				token.tokenId = to.getPath();
//				dest.setLockToken(token); 
//			}
		}
		else
		{
			throw new OpenEditRuntimeException("this shouldn't happen - this isn't a repository item");
		}
	}

	public LockResult lock(LockTimeout inLockTimeout, LockInfo inLockInfo)
	{
		LockToken token = new LockToken(getPage().getPath(), inLockInfo, inLockTimeout);
		log.info("locked by " + inLockInfo.owner + " for: " + getPage().getPath() + " " + hashCode());
		
		setCurrentLock(token);
		
		return LockResult.success(token);
	}

	public LockResult refreshLock(String inPath)
	{		
		if( getCurrentLock() == null)
		{
			//if it wasn't locked to begin with (what about child files)
			return LockResult.failed(FailureReason.PRECONDITION_FAILED);
		}
		LockInfo info = getCurrentLock().info;
		log.info("refreshed by " + info.owner + " for: " + getPage().getPath());
		
		return LockResult.success(getCurrentLock());
	}

	public void unlock(String inArg0)
	{
		//Might already be unlocked
		if( getCurrentLock() != null)
		{
		
			log.info("unlocked " + getPage().getPath() + " owned by " + getCurrentLock().info.owner);
			
		}
		setCurrentLock(null);

	}

	public void moveTo(CollectionResource newParent, String newName) 
	{
		if (newParent instanceof OpenEditResource)
		{
			OpenEditResource targetFolder = (OpenEditResource) newParent;

			getResourceFactory().moveTo( this, targetFolder, newName);
			
		}
		else
		{
			throw new OpenEditRuntimeException("this shouldn't happen - this isn't a repository item");
		}
	}

	public void delete()
	{
		log.info("Deleted " + getPage());
		getResourceFactory().delete(this);
	}

	public Date getCreateDate()
	{
		return getPage().getLastModified();
	}

	public OpenEditResourceFactory getResourceFactory()
	{
		return fieldResourceFactory;
	}

	public void setResourceFactory(OpenEditResourceFactory inResourceFactory)
	{
		fieldResourceFactory = inResourceFactory;
	}

	public void setProperties(Fields inFields)
	{
		
		for (Iterator iterator = inFields.setFields.iterator(); iterator.hasNext();)
		{
			SetField type = (SetField) iterator.next();
			getPage().getPageSettings().setProperty(type.name, type.value);
			
		}
		for (Iterator iterator = inFields.removeFields.iterator(); iterator.hasNext();)
		{
			Field type = (Field) iterator.next();
			getPage().getPageSettings().removeProperty(type.name);
		}
		//TODO: Save this thing
		
	}
	
}
