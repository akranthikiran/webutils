package com.fw.webutil.common;

public class FlagUtil
{
	public static int setFlag(int flags, int FLAG_BIT, boolean flagVal)
	{
		if(flagVal)
		{
			return (flags | FLAG_BIT);
		}
		
		return (flags & (~FLAG_BIT));
	}
	
	public static boolean getFlag(int flags, int FLAG_BIT)
	{
		return ((flags & FLAG_BIT) == FLAG_BIT);
	}

}
