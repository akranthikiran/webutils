package com.fw.webutil.common.model.dynamic;

public class Property
{
	private String name;
	private Object value;
	private String jsonValue;
	
	public Property()
	{
	}

	public Property(String name, Object value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public String getJsonValue()
	{
		return jsonValue;
	}

	public void setJsonValue(String jsonValue)
	{
		this.jsonValue = jsonValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj == this)
		{
			return true;
		}

		if(!(obj instanceof Property))
		{
			return false;
		}

		Property other = (Property)obj;
		return name.equals(other.name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashcode()
	 */
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("Name: ").append(name);
		builder.append(",").append("Value: ").append(value);

		builder.append("]");
		return builder.toString();
	}
}
