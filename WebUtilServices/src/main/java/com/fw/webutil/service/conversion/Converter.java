package com.fw.webutil.service.conversion;

public interface Converter<S, T>
{
	public T convert(String property, S source);
}
