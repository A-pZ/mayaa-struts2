package org.seasar.mayaa.struts2;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.impl.MayaaServlet;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * Mayaaで処理を行うResultです。
 */
public class MayaaResult extends StrutsResultSupport {
	/** シリアルバージョンID */
	private static final long serialVersionUID = -7752483670104351387L;

	/**
	 * 出力処理を行います。
	 * 
	 * @param location
	 *            出力に使用するファイルパス
	 * @param actionInvocation
	 *            ActionInvocation
	 */
	protected void doExecute(String location, ActionInvocation actionInvocation)
			throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		String path = request.getServletPath();
		if (path.lastIndexOf('/') > 0) {
			path = path.substring(1, path.lastIndexOf('/') + 1);
		} else {
			path = "";
		}

		if (location != null) {
			if (location.startsWith("/")) {
				// 絶対パス指定
				request = new MayaResultRequest(request, location);
			} else {
				// 相対パス指定
				request = new MayaResultRequest(request, path + location);
			}
		} else {
			// location指定が無い場合「action名+".html"」
			request = new MayaResultRequest(request, path
					+ ActionContext.getContext().getName() + ".html");
		}

		CycleUtil.initialize(request, ServletActionContext.getResponse());
		Engine engine = ProviderUtil.getEngine();
		setupCharacterEncoding(ServletActionContext.getRequest(), engine
				.getParameter("requestCharacterEncoding"));
		engine.doService(null, true);
	}

	protected void setupCharacterEncoding(HttpServletRequest request,
			String encoding) {
		if (request.getCharacterEncoding() == null) {
			try {
				request.setCharacterEncoding(encoding);
			} catch (UnsupportedEncodingException e) {
				String message = StringUtil.getMessage(MayaaServlet.class, 0,
						encoding);
				Log log = LogFactory.getLog(MayaaServlet.class);
				log.warn(message, e);
			}
		}
	}

	/**
	 * PathInfoを偽装するHttpServletRequest
	 */
	public static class MayaResultRequest extends HttpServletRequestWrapper {
		private String location;

		public MayaResultRequest(HttpServletRequest request, String location) {
			super(request);
			this.location = location;
		}

		public String getPathInfo() {
			return location;
		}

		public String getServletPath() {
			return "";
		}
	}
}
