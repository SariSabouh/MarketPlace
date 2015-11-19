package cs499.exceptions;

public class ItemUseException extends Exception{
	
    /**
	 * Exception that is called if the @{link Item} using failed.
	 */
	private static final long serialVersionUID = 1L;

	public ItemUseException() {}

    public ItemUseException(String message)
    {
       super(message);
    }
}
