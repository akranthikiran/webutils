package com.fw.webutil.service.conversion;

class ConverterKey
{
	private Class<?> from;
	private Class<?> to;
	
	public ConverterKey(Class<?> from, Class<?> to)
	{
		this.from = from;
		this.to = to;
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

		if(!(obj instanceof ConverterKey))
		{
			return false;
		}

		ConverterKey other = (ConverterKey)obj;
		return from.equals(other.from) && to.equals(other.to);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashcode()
	 */
	@Override
	public int hashCode()
	{
		return from.hashCode() + to.hashCode();
	}
}
