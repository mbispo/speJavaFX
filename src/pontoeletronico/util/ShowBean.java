package pontoeletronico.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;


public class ShowBean {

	/**
	 * Print all properties for a given bean. It's useful for overwriting
	 * toString method.
	 * 
	 * @param bean
	 *            Object to get all properties from.
	 * @param stopClass
	 * @param showNulls
	 *            Determine if you wether you want to show null properties or
	 *            not.
	 * @return String representing bean state.
	 * @author andres santana
	 */

	public static String show(Object bean, Class stopClass, boolean showNulls) {
		if (bean == null)
			return null;
		StringBuilder sb = new StringBuilder(bean.getClass().getSimpleName()).append("[ ");
		try {
			BeanInfo bi = Introspector.getBeanInfo(bean.getClass(), stopClass);
			PropertyDescriptor[] pd = bi.getPropertyDescriptors();

			for (int i = 0; i < pd.length; i++) {
				if (!"class".equals(pd[i].getName())) {

					Object result = pd[i].getReadMethod().invoke(bean);
					
					if (showNulls || result != null) {

						sb.append(pd[i].getDisplayName()).append(" = ").append(String.valueOf(result));

						if (i == pd.length - 1)
							continue;
						sb.append(", ");
					}
				}
			}
		} catch (Exception ex) {
		}

		return sb.append(" ]").toString();
		
	}
	
	public static String show(Object bean) {
		return show(bean, null, false);
	}
	

}
