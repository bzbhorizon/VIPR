/**
 * 
 */
package net.dapper.scrender;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;

/**
 * @author Ohad Serfaty
 *
 */
public class RandomTitleListener implements TitleListener {

	private final double randomNumber;
	private final Lock lock;
	private final Browser browser;

	/**
	 * 
	 * 
	 * @param randomNumber
	 * @param lock
	 * @param browser
	 */
	public RandomTitleListener(double randomNumber , Lock lock  , Browser browser) {
		this.randomNumber = randomNumber;
		this.lock = lock;
		this.browser = browser;
	}

	public void changed(TitleEvent arg0) 
	{
		if (arg0.title.equalsIgnoreCase(Double.toString(randomNumber)))
		{
			browser.removeTitleListener(this);
			lock.release();
		}
	}

}
