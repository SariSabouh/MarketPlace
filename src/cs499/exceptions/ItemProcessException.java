package cs499.exceptions;

public class ItemProcessException extends Exception{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ItemProcessException() {}

    public ItemProcessException(String message)
    {
       super(message);
    }
}
