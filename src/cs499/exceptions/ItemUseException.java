package cs499.exceptions;

public class ItemUseException extends Exception{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ItemUseException() {}

    public ItemUseException(String message)
    {
       super(message);
    }
}
