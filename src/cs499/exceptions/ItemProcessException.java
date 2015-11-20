package cs499.exceptions;

public class ItemProcessException extends Exception{
	
    /**
     * @author SabouhS
     * 
	 *  Excpetion that is called if the @{link Item} purchasing failed.
	 */
	private static final long serialVersionUID = 1L;

	public ItemProcessException() {}

    public ItemProcessException(String message)
    {
       super(message);
    }
}
