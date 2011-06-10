package org.openedit.webdav;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.DefaultResponseHandler;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;
import com.openedit.Generator;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.generators.BaseGenerator;
import com.openedit.generators.Output;
import com.openedit.page.Page;
import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

public class WebDavGenerator extends BaseGenerator implements Generator
{
	private static final Log log = LogFactory.getLog(WebDavGenerator.class);
	//http://www.ietf.org/rfc/rfc2518.txt
	//http://www.digital-arcanist.com/sanctum/article.php?story=20070427101250622
	protected HttpManager fieldHttpManager;
	protected OpenEditResourceFactory fieldOpenEditResourceFactory;
	
	public OpenEditResourceFactory getOpenEditResourceFactory()
	{
		return fieldOpenEditResourceFactory;
	}


	public void setOpenEditResourceFactory(OpenEditResourceFactory inOpenEditResourceFactory)
	{
		fieldOpenEditResourceFactory = inOpenEditResourceFactory;
	}


	public HttpManager getHttpManager(String inWebApp)
	{
		if (fieldHttpManager == null)
		{
			if( inWebApp != null && inWebApp.endsWith("/") )
			{
				inWebApp = inWebApp.substring(0,inWebApp.length() - 1);
			}
			getOpenEditResourceFactory().setWebApp(inWebApp);
			fieldHttpManager = new HttpManager(getOpenEditResourceFactory(), new DefaultResponseHandler("1,2"));
		}
		return fieldHttpManager;
	}
	public void setHttpManager(HttpManager inHttpManager)
	{
		fieldHttpManager = inHttpManager;
	}
	public boolean canGenerate(WebPageRequest inReq)
	{
		return true;
	}
	public void generate(WebPageRequest inContext, Page inPage, Output inOut) throws OpenEditException
	{
		HttpServletRequest req = inContext.getRequest();
		//req.get
		
        HttpServletResponse resp = inContext.getResponse();
        Request request = new ServletRequest(req);
        //String username =         request.getAuthorization().getUser()
        Response response = new ServletResponse(resp);
        
        //TODO: there are no cookies with webdav so we will need to find another way to cache these user requests
        HttpManager manager = fieldHttpManager;//(HttpManager)inContext.getSessionValue("webdavmanager");

        if( manager == null)
        {
        	manager = getHttpManager(req.getContextPath());
        	
           // inContext.putSessionValue("webdavmanager", manager);
        	fieldHttpManager = manager;
        }
        
		log.info("generate: " + inPage.getPath());
        manager.process(request, response);
	
	}
	public String getName()
	{
		return "OpenEdit WEBDAV generator";
	}
}
