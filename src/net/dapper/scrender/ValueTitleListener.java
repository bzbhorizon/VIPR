/**
 * 
 */
package net.dapper.scrender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;

/**
 * @author Ohad Serfaty
 *
 */
/**
 * @author Ohad Serfaty
 *
 */
public class ValueTitleListener implements TitleListener {

	private final double randomNumber;
	private String value=null;
	private final Lock lock;
	private final Browser browser;

	/**
	 * @param randomNumber
	 * @param script 
	 * @param lock 
	 */
	public ValueTitleListener(int randomNumber, String script, Lock lock , Browser browser) {
		this.randomNumber = randomNumber;
		this.lock = lock;
		this.browser = browser;
	}

	public void changed(TitleEvent arg0) 
	{
		Pattern pattern = Pattern.compile("ValueTitle:"+randomNumber + ":(.*)");
		Matcher matcher = pattern.matcher(arg0.title);
		if (matcher.find())
		{
			browser.removeTitleListener(this);
			String theValue = matcher.group(1);
			this.setValue(theValue);
			lock.release();
		}
		else
		{
			browser.removeTitleListener(this);
			lock.release();
		}
	}

	/**
	 * @param string
	 */
	private void setValue(String value) {
		this.value = value;
	}

	
	/**
	 * @param script
	 * @return
	 */
	public String getScript(String script) 
	{
		return "document.title='ValueTitle:"+randomNumber+":'+"+script;
	}

	/**
	 * @return
	 */
	public String getValue() 
	{
		return this.value;
	}

}

