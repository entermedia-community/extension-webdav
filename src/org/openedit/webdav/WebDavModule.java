package org.openedit.webdav;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bradmcevoy.http.DefaultResponseHandler;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;
import com.openedit.WebPageRequest;
import com.openedit.modules.BaseModule;

public class WebDavModule extends BaseModule
{
	
	protected HttpManager fieldHttpManager;
	protected OpenEditResourceFactory fieldOpenEditResourceFactory;
	
	public void generateWebDav(WebPageRequest inReq){
		HttpServletRequest req = (HttpServletRequest) inReq.getRequest();
		//req.get
		
        HttpServletResponse resp = (HttpServletResponse) inReq.getResponse();
        Request request = new ServletRequest(req);
        Response response = new ServletResponse(resp);
      
        getHttpManager().process(request, response);
		
	}
	public OpenEditResourceFactory getOpenEditResourceFactory()
	{
		return fieldOpenEditResourceFactory;
	}


	public void setOpenEditResourceFactory(OpenEditResourceFactory inOpenEditResourceFactory)
	{
		fieldOpenEditResourceFactory = inOpenEditResourceFactory;
	}


	public HttpManager getHttpManager()
	{
		if (fieldHttpManager == null)
		{
			fieldHttpManager = new HttpManager(getOpenEditResourceFactory(), new DefaultResponseHandler());
		}
		return fieldHttpManager;
	}
	public void setHttpManager(HttpManager inHttpManager)
	{
		fieldHttpManager = inHttpManager;
	}
}
