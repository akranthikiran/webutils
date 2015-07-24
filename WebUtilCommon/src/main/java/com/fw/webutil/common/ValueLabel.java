package com.fw.webutil.common;

public class ValueLabel
{
	private String value;
	private String label;
	
	public ValueLabel()
	{
	}

	public ValueLabel(String value, String label)
	{
		this.value = value;
		this.label = label;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Value: ").append(value);
		builder.append(",").append("Lable: ").append(label);

		builder.append("]");
		return builder.toString();
	}
}
