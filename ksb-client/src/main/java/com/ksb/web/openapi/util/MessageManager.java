package com.ksb.web.openapi.util;

import java.util.Locale;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

public class MessageManager {

	public static final String MESSAGE_NAME = "MessageSource";

	private static final Logger log = LogManager.getLogger("appServer");

	private static MessageSource msg = null;

	public static void init(MessageSource source) {
		msg = source;
	}

	public void setMessageSource(MessageSource source) {
		MessageManager.msg = source;
	}

	public static String getMessage(String key) {
		try {
			return getMessage(key, Locale.getDefault());
		} catch (Exception e) {
			log.warn("Can not find the key in property file: " + key);
		}

		return null;
	}

	public static String getMessage(String key, Locale locale) {
		try {
			if (msg != null) {
				return msg.getMessage(key, null, locale);
			} else {
				log.warn("The message source is null: " + key);
			}
		} catch (Exception e) {
			log.warn("Can not find the key in property file: " + key);
		}

		return null;
	}

	public static String getString(String key, String def) {
		String value = getMessage(key);
		if (value != null)
			return value;
		else
			return def;
	}

	public static String getString(String key) {
		String value = getMessage(key);
		if (log.isTraceEnabled()) {
			log.trace(key + "=" + value);
		}
		return value != null ? value.trim() : "";
	}

	public static String getStringLn(String key) {
		String value = getMessage(key);
		return value != null ? value.trim() + "\n" : "";
	}

	public static short getShort(String key, short def) {
		String value = getMessage(key);
		if (value != null) {
			try {
				return Short.valueOf(value).shortValue();
			} catch (NumberFormatException ec) {
				log.warn("Data Format error:" + value);
			}
		}
		return def;
	}

	public static int getInt(String key, int def) {
		String value = getMessage(key);
		if (value != null) {
			try {
				return Integer.valueOf(value).intValue();
			} catch (NumberFormatException ec) {
				log.warn("Data Format error:" + key + "=" + value);
			}
		}
		return def;
	}

	public static int getInt(String key) {
		String value = getMessage(key);
		if (value != null) {
			try {
				return Integer.valueOf(value).intValue();
			} catch (NumberFormatException ec) {
				log.warn("Data Format error:" + key + "=" + value);
			}
		}

		return 0;
	}

	public static long getLong(String key, long def) {
		String value = getMessage(key);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ec) {
				log.warn("Data Format error:" + value);
			}
		}
		return def;
	}

	public static boolean getBoolean(String key, boolean def) {
		String value = getMessage(key);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException ec) {
				log.warn("Data Format error:" + value);
			}
		}
		return def;
	}

	public static int getIntByEncodeString(String key, int def) {
		String value = getMessage(key);
		if (value != null) {
			try {
				return Integer.decode(value);
			} catch (NumberFormatException ec) {
				log.warn("Data Format error:" + value);
			}
		}
		return def;
	}

}
