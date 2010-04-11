/**
 * 
 */
package net.dapper.scrender;

/**
 * @author Ohad Serfaty
 *
 * An exception that is throws by the renderer when an error occurs on the rendering process.
 *
 */
public class RenderingException extends Exception 
{

	private static final long serialVersionUID = 1L;

	public RenderingException(String message){
		super(message);
	}
	
	public RenderingException(Throwable t){
		super(t);
	}
	
	public RenderingException(String message,Throwable t ){
		super(message , t);
	}
	
}
