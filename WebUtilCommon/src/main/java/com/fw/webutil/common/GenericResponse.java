package com.fw.webutil.common;

public class GenericResponse
{
	public static final GenericResponse SUCCESS = new GenericResponse(IResponseCodes.SUCCESS, null, "success");
	
	private String code;
	private String message;
	private String field;
	private String value;

	public GenericResponse(String code, String field, String message)
	{
		this.code = code;
		this.field = field;
		this.message = message;
	}

	public GenericResponse(String message)
	{
		this.code = IResponseCodes.SUCCESS;
		this.message = message;
	}

	public String getCode()
	{
		return code;
	}
	
	public String getField()
	{
		return field;
	}

	public String getMessage()
	{
		return message;
	}
	
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Code: ").append(code);
		builder.append(",").append("Field: ").append(field);
		builder.append(",").append("Message: ").append(message);

		builder.append("]");
		return builder.toString();
	}
}
