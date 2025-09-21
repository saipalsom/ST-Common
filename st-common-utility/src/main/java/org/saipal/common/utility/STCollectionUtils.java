package org.saipal.common.utility;

import java.util.Collection;
import java.util.Map;

public class STCollectionUtils {

	public static boolean isEmpty(Collection<?> list) {
		return list == null || list.size() == 0;
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.size() == 0;
	}
}
