/*
 * Created on 10/08/2004
 */
package net.dapper.scrender;

/**
 * @author anat, Ohad Serfaty
 */
public class Lock
{
	
	private boolean done=false;

	public void release()
	{
		done = true;
		synchronized(this)
		{
			this.notify();
		}
	}

	public boolean  waitFor()
	{
		return this.waitFor(Long.MAX_VALUE);
	}
	
	/**
	 * Wait for the lock to be release for the specified timeout.
	 * 
	 * @param milisecondsToWait
	 * @return true if the lock was released before the timeout. false otherwise.
	 */
	public boolean waitFor(long milisecondsToWait)
	{
		long startTime = System.currentTimeMillis();
		long timeLeft = 0;
		boolean result = true;
		synchronized(this)
		{
			while (done==false && (result = ( (timeLeft = startTime + milisecondsToWait -  System.currentTimeMillis()) >=0)))
			try 
			{
				//System.out.println("Waiting for :" + timeLeft);
				this.wait(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if (done == true)
			result = true;
		done = false;
		//System.out.println("Finished waiting. result:"+result);
		return result;
	}
}
