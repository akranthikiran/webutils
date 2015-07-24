package com.fw.webutil.model.dynamic.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fw.ccg.xml.XMLBeanParser;
import com.fw.webutil.common.model.dynamic.ValidatorConfiguration;
import com.fw.webutil.common.validator.annotations.Global;
import com.fw.webutil.model.dynamic.IDataSpecifier;

@Component("annotationResolver")
public class ValidatorConfiguraionFactory
{
	public static class ResolverDetails
	{
		private Class<? extends Annotation> type;
		private Class<?> targetType;
		private Map<String, String> valueAttrs = new HashMap<>();
		private String name;

		public Class<? extends Annotation> getType()
		{
			return type;
		}

		public void setType(Class<? extends Annotation> type)
		{
			this.type = type;
		}

		public Class<?> getTargetType()
		{
			return targetType;
		}

		public void setTargetType(Class<?> targetType)
		{
			this.targetType = targetType;
		}

		public Map<String, String> getValueAttrs()
		{
			return valueAttrs;
		}

		public void setValueAttrs(String valueAttr)
		{
			String attrLst[] = valueAttr.split("\\s*\\,\\s*");
			
			for(String attr: attrLst)
			{
				valueAttrs.put(attr, null);
			}
		}
		
		public void addAttribute(String attr, String value)
		{
			valueAttrs.put(attr, value);
		}

		public String getName()
		{
			return name;
		}

		public void setName(String simpleName)
		{
			this.name = simpleName;
		}
	}
	
	private static class ResolverKey
	{
		Class<? extends Annotation> annotationType;
		Class<?> targetType;
		//flag indicating if this key is from actual configuration or key built for search
		boolean actualConfiguration;
		
		public ResolverKey(Class<? extends Annotation> annotationType, Class<?> targetType, boolean actualConfiguration)
		{
			this.annotationType = annotationType;
			this.targetType = targetType;
			this.actualConfiguration = actualConfiguration;
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

			if(!(obj instanceof ValidatorConfiguraionFactory.ResolverKey))
			{
				return false;
			}
			
			ResolverKey actual = null, other = null;
			
			if(actualConfiguration)
			{
				actual = this;
				other = (ResolverKey)obj;
			}
			else
			{
				actual = (ResolverKey)obj;
				other = this;
			}
			
			return (actual.annotationType.equals(other.annotationType) && (actual.targetType.isAssignableFrom(other.targetType)));
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashcode()
		 */
		@Override
		public int hashCode()
		{
			/*
			 * target type is not considered in hash code. Reason is two ResolverKeys are considered equal
			 * even if the target types are compatible (they need not be equal).
			 */
			return annotationType.hashCode();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder(super.toString());
			builder.append("[");

			builder.append("Annotation: ").append(annotationType.getSimpleName());
			builder.append(",").append("Target: ").append(targetType.getSimpleName());
			builder.append(",").append("Actual: ").append(actualConfiguration);
			builder.append(",").append("Hash: ").append(hashCode());

			builder.append("]");
			return builder.toString();
		}
	}
	
	private Map<ResolverKey, ResolverDetails> detailsMap = new HashMap<>();
	
	public ValidatorConfiguraionFactory()
	{
		XMLBeanParser.parse(ValidatorConfiguraionFactory.class.getResourceAsStream("resolver-configuration.xml"), this);
	}

	public void addResolver(ResolverDetails resolver)
	{
		detailsMap.put(new ResolverKey(resolver.getType(), resolver.getTargetType(), true), resolver);
	}

	private Object getAttribute(Annotation annotation, String name, boolean ignoreError)
	{
		try
		{
			Class<?> annotationClass = annotation.getClass();
			Method method = annotationClass.getDeclaredMethod(name);
			return method.invoke(annotation);
		}catch(Exception ex)
		{
			if(ignoreError)
			{
				return null;
			}
			
			throw new IllegalStateException("An error occurred while resoving annotation: " + annotation, ex);
		}
	}
	
	public Collection<ValidatorConfiguration> getValidatorConfigurations(Class<?> parentClass, IDataSpecifier field)
	{
		Annotation annotations[] = field.getAnnotations();
		
		if(annotations == null || annotations.length == 0)
		{
			return Collections.emptyList();
		}
		
		ResolverKey resolverKey = null;
		ResolverDetails resolverDetails = null;
		
		List<ValidatorConfiguration> validatorConfigurations = new ArrayList<>();
		ValidatorConfiguration validatorConfiguration = null;
		Object value = null;
		
		for(Annotation annotation: annotations)
		{
			resolverKey = new ResolverKey(annotation.annotationType(), field.getType(), false);
			resolverDetails = detailsMap.get(resolverKey);
			
			if(resolverDetails == null)
			{
				continue;
			}
			
			validatorConfiguration = new ValidatorConfiguration(resolverDetails.name);
			
			for(String attr: resolverDetails.valueAttrs.keySet())
			{
				value = resolverDetails.valueAttrs.get(attr);
				
				if(value == null)
				{
					value = getAttribute(annotation, attr, false);
				}
				
				validatorConfiguration.setValue(attr, value);
			}
			
			validatorConfiguration.setGlobal(annotation.getClass().getAnnotation(Global.class) != null);
			validatorConfiguration.setErrorMessage((String)getAttribute(annotation, "message", true));
			
			validatorConfigurations.add(validatorConfiguration);
		}
		
		return validatorConfigurations;
	}
}
