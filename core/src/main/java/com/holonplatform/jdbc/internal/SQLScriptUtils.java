/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.jdbc.internal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * @author BODSI08
 *
 */
public class SQLScriptUtils {

	private static final Logger LOGGER = JdbcLogger.create();

	/**
	 * Statement separator
	 */
	private static final String STATEMENT_SEPARATOR = ";";

	/**
	 * Prefix for single-line comments
	 */
	private static final String COMMENT_PREFIX = "--";

	/**
	 * Start delimiter for block comments
	 */
	private static final String BLOCK_COMMENT_START = "/*";

	/**
	 * End delimiter for block comments
	 */
	private static final String BLOCK_COMMENT_END = "*/";

	public static void executeSqlScript(Connection connection, String script) throws IOException {

		ObjectUtils.argumentNotNull(connection, "Connection must be not null");
		ObjectUtils.argumentNotNull(script, "Script must be not null");

		try {

			final List<String> statements = splitSql(script);

			@SuppressWarnings("resource")
			Statement stmt = connection.createStatement();
			try {
				for (String statement : statements) {
					stmt.execute(statement);
				}
			} finally {
				try {
					stmt.close();
				} catch (Throwable ex) {
					LOGGER.debug(() -> "Failed to close JDBC Statement", ex);
				}
			}

			LOGGER.info("SQL script executed");

		} catch (Exception ex) {
			throw new IOException("Failed to execute SQL script", ex);
		}
	}

	/**
	 * Split given SQL script into separate statements.
	 * @return the individual statements
	 * @throws IOException if an error occurred
	 */
	public static List<String> splitSql(String script) throws IOException {

		final List<String> statements = new ArrayList<>();

		StringBuilder sb = new StringBuilder();

		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		boolean inEscape = false;
		for (int i = 0; i < script.length(); i++) {
			char c = script.charAt(i);
			if (inEscape) {
				inEscape = false;
				sb.append(c);
				continue;
			}
			if (c == '\\') {
				inEscape = true;
				sb.append(c);
				continue;
			}
			if (!inDoubleQuote && (c == '\'')) {
				inSingleQuote = !inSingleQuote;
			} else if (!inSingleQuote && (c == '"')) {
				inDoubleQuote = !inDoubleQuote;
			}
			if (!inSingleQuote && !inDoubleQuote) {

				if (script.startsWith(STATEMENT_SEPARATOR, i)) {
					// end of statement
					if (sb.length() > 0) {
						statements.add(sb.toString());
						sb = new StringBuilder();
					}
					i += STATEMENT_SEPARATOR.length() - 1;
					continue;
				} else if (script.startsWith(COMMENT_PREFIX, i)) {
					// single line comment
					int indexOfNextNewline = script.indexOf("\n", i);
					if (indexOfNextNewline > i) {
						i = indexOfNextNewline;
						continue;
					} else {
						break;
					}
				} else if (script.startsWith(BLOCK_COMMENT_START, i)) {
					// block comment
					int indexOfCommentEnd = script.indexOf(BLOCK_COMMENT_END, i);
					if (indexOfCommentEnd > i) {
						i = indexOfCommentEnd + BLOCK_COMMENT_START.length() - 1;
						continue;
					} else {
						throw new IOException("Missing block comment end delimiter at: " + i);
					}
				} else if (c == ' ' || c == '\n' || c == '\t') {
					// trim whitespaces into a single space
					if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
						c = ' ';
					} else {
						continue;
					}
				}
			}
			sb.append(c);
		}

		final String stmt = sb.toString();
		if (!stmt.trim().equals("")) {
			statements.add(stmt);
		}

		return statements;
	}

}
