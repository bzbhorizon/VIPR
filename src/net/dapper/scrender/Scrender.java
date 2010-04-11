/**
 * 
 */
package net.dapper.scrender;

import java.io.File;

/**
 * @author Ohad Serfaty
 *
 */
public class Scrender
{

	private final boolean disposeOnFinish;
	private RenderingBrowser browser;

	public Scrender(boolean disposeOnFinish)
	{
		this.disposeOnFinish = disposeOnFinish;
		this.browser = new RenderingBrowser();
	}
	
	/**
	 * 
	 */
	public Scrender()
	{
		this(true);
	}

	public void init() throws Exception
	{
		this.browser.init();
	}
	
	public void render(String url , File saveLocation) throws RenderingException
	{
		this.browser.render(url, saveLocation);
		if (this.disposeOnFinish)
			this.browser.dispose();
	}
	
	public void dispose()
	{
		this.browser.dispose();
	}
	
	public void resetBrowser()
	{
		this.browser.resetBrowser(true);
	}

}
