/***********************************************************************
 * OPENSCHEMA
 * An open source implementation of document structuring schemata.
 *
 * Copyright (C) 2004-2013 Pablo Ariel Duboue <pablo.duboue@gmail.com>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111, USA.
 ***********************************************************************/

package net.sf.openschema.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A filter stream that takes schemas defined in the OpenSchema DSL and transforms it to XML in the OpenSchema XSD.
 * 
 * @author Pablo Ariel Duboue <pablo.duboue@gmail.com>
 */

public class SchemaToXmlFilterStream extends FilterInputStream {

	public SchemaToXmlFilterStream(InputStream in) {
		super(new ByteArrayInputStream(transform(in)));
	}

	// all magic happens here
	private static byte[] transform(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			List<String> text = new ArrayList<String>();
			StringBuilder longLine = new StringBuilder();
			while (line != null) {
				line = line.replaceAll("\\;.*$", "");
				if (!line.matches("^\\s*$")) {
					if (line.matches("\\\\\\s*$")) {
						longLine.append(line.replaceAll("\\\\\\s*$", " "));
					} else if (longLine.length() > 0) {
						text.add(longLine.append(' ').append(line).toString());
						longLine.setLength(0);
					} else {
						if (!line.matches("^\\s*\\;"))
							text.add(line);
					}
				}

				line = br.readLine();

			}
			br.close();

			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			sb.append("<OpenSchema \n");
			sb.append("  xmlns=\"http://openschema.sf.net\"\n");
			sb.append("  xmlns:fd=\"http://jfuf.sf.net/FD\">\n");
			for (int currentLine = 0; currentLine < text.size();) {
				line = text.get(currentLine);
				int spaces = countSpaces(line);
				if (line.matches("^\\s*predicate.*"))
					currentLine = parsePredicate(sb, spaces, currentLine, text);
				else if (line.matches("^\\s*schema.*"))
					currentLine = parseSchema(sb, spaces, currentLine, text);
				else
					throw new RuntimeException("Syntax error line: " + currentLine + ", '" + line + "'");
			}
			sb.append("</OpenSchema>\n");

			return sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	private static int countSpaces(String line) {
		int spaces = 0;
		while (spaces < line.length() && line.charAt(spaces) == ' ')
			spaces++;
		return spaces;
	}

	private static int parsePredicate(final StringBuilder sb, int indent, int currentLine, final List<String> text) {
		String line = text.get(currentLine);
		String name = line.substring(line.indexOf("predicate") + "predicate".length()).trim();
		sb.append("<Predicate ID=\"").append(name).append("\">\n");
		currentLine = parseInsidePredicate(sb, indent, currentLine + 1, text);
		sb.append("</Predicate>\n");
		return currentLine;
	}

	private static int parseInsidePredicate(final StringBuilder sb, int thrIndent, int currentLine,
			final List<String> text) {
		String line = text.get(currentLine);
		int indent = countSpaces(line);
		while (currentLine < text.size() && indent > thrIndent) {
			if (line.matches(".*variables.*"))
				currentLine = parseInsideVar(sb, indent, currentLine + 1, text);
			else if (line.matches(".*properties.*"))
				currentLine = parseInsideProp(sb, indent, currentLine + 1, text);
			else if (line.matches(".*output.*"))
				currentLine = parseInsideOutput(sb, indent, currentLine + 1, text);
			else
				throw new RuntimeException("Syntax error line: " + currentLine + ", '" + line + "'");

			if (currentLine >= text.size())
				break;
			line = text.get(currentLine);
			indent = countSpaces(line);
		}
		return currentLine;
	}

	private static final Pattern VAR_RE = Pattern.compile("\\s*(req\\s+)?(def\\s+)?([^\\s]+)\\s*:\\s*([^\\s]+)\\s*");

	private static int parseInsideVar(final StringBuilder sb, int thrIndent, int currentLine, final List<String> text) {
		String line = text.get(currentLine);
		int indent = countSpaces(line);
		while (currentLine < text.size() && indent > thrIndent) {
			Matcher m = VAR_RE.matcher(line);
			if (!m.find())
				throw new RuntimeException(line);
			boolean req = m.group(1) != null;
			boolean def = m.group(2) != null;
			String name = m.group(3);
			String type = m.group(4);
			sb.append("<Variable ID=\"" + name + "\" Type=\"" + type + "\" Required=\"" + (req ? "true" : "false")
					+ "\" DefaultFocus=\"" + (def ? "true" : "false") + "\"/>\n");
			currentLine++;
			if (currentLine >= text.size())
				break;
			line = text.get(currentLine);
			indent = countSpaces(line);
		}
		return currentLine;
	}

	private static int parseInsideProp(final StringBuilder sb, int thrIndent, int currentLine, final List<String> text) {
		String line = text.get(currentLine);
		int indent = countSpaces(line);
		while (currentLine < text.size() && indent > thrIndent) {
			sb.append("<Property Value=\"" + line.trim() + "\"/>\n");
			currentLine++;
			if (currentLine >= text.size())
				break;
			line = text.get(currentLine);
			indent = countSpaces(line);
		}
		return currentLine;
	}

	private static int parseInsideOutput(final StringBuilder sb, int thrIndent, int currentLine, final List<String> text) {
		String line = text.get(currentLine);
		int indent = countSpaces(line);
		sb.append("<Output>\n");
		sb.append("<fd:FD>\n");
		int currentIndent = indent;
		Stack<Integer> indentList = new Stack<Integer>();
		while (currentLine < text.size() && indent > thrIndent) {
			if (indent < currentIndent) {
				int otherIndent = indentList.pop();
				sb.append("</fd:V>\n");
				while (!indentList.isEmpty() && indent < otherIndent) {
					otherIndent = indentList.pop();
					sb.append("</fd:V>\n");
				}
			}
			currentIndent = indent;
			String[] parts = line.trim().split("\\s+", 2);
			String attr = parts[0];
			String value = parts.length > 1 ? parts[1] : null;
			sb.append("<fd:V N=\"" + attr + "\">");
			if (value != null)
				sb.append("<fd:G>" + value.trim() + "</fd:G></fd:V>\n");
			else {
				sb.append("\n");
				indentList.push(indent);
			}
			currentIndent = indent;

			currentLine++;
			if (currentLine >= text.size())
				break;
			line = text.get(currentLine);
			indent = countSpaces(line);
		}
		for (int i = 0; i < indentList.size(); i++)
			sb.append("</fd:V>\n");

		sb.append("</fd:FD>\n");
		sb.append("</Output>\n");
		return currentLine;
	}

	private static int parseSchema(final StringBuilder sb, int thrIndent, int currentLine, final List<String> text) {
		String line = text.get(currentLine);
		int indent = countSpaces(line);
		String name = line.replace("predicate ", "").trim();
		sb.append("<Schema ID=\"" + name + "\">\n");
		currentLine = parseInsideSchema(sb, indent, currentLine + 1, text);
		sb.append("</Schema>\n");
		return currentLine;
	}

	private static int parseInsideSchema(final StringBuilder sb, int thrIndent, int currentLine, final List<String> text) {
		String line = text.get(currentLine);
		int indent = countSpaces(line);

		int currentIndent = indent;
		Stack<Integer> indentStack = new Stack<Integer>();
		Stack<String> open = new Stack<String>();
		while (currentLine < text.size() && indent > thrIndent) {
			if (indent < currentIndent) {
				if (open.isEmpty()) {
					for (int l = Math.max(0, currentLine - 5); l < Math.min(text.size(), currentLine + 5); l++)
						System.err.println((l == currentLine ? "=> " : "   ") + text.get(l));
					throw new RuntimeException("open is empty");
				}
				int otherIndent = indentStack.pop();
				String operator = open.pop();
				sb.append("</").append(operator).append("></Node>\n");
				while (!indentStack.isEmpty() && indent < otherIndent) {
					otherIndent = indentStack.pop();
					operator = open.pop();
					sb.append("</").append(operator).append("></Node>\n");
				}
			}
			currentIndent = indent;
			if (line.matches(".*aggregation-boundary.*"))
				sb.append("<Node><AggrBoundary/></Node>\n");
			else if (line.matches(".*paragraph-boundary.*"))
				sb.append("<Node><ParBoundary/></Node>\n");
			else if (line.matches(".*((choice)|(sequence)|(star)|(plus)|(optional)).*")) {
				String operator = "Sequence";
				if (line.matches(".*choice.*"))
					operator = "Choice";
				else if (line.matches(".*sequence.*"))
					operator = "Sequence";
				else if (line.matches(".*optional.*"))
					operator = "Optional";
				else if (line.matches(".*star.*"))
					operator = "KleeneStar";
				else if (line.matches(".*plus.*"))
					operator = "KleenePlus";

				sb.append("<Node>");
				sb.append('<').append(operator).append(">\n");
				open.push(operator);
				indentStack.push(indent);
			} else {
				if (line.indexOf('(') < 0)
					throw new RuntimeException(line);

				String[] parts = line.trim().split("\\(", 2);
				String predicate = parts[0].trim();
				String variableBindings = parts.length > 1 ? parts[1].replace(")", "") : null;
				sb.append("<Node><Predicate Name=\"" + predicate + "\">\n");
				if (variableBindings != null && !variableBindings.trim().isEmpty()) {
					String[] varBindPairs = variableBindings.split(",");
					for (String varBindPair : varBindPairs) {
						String[] varBinding = varBindPair.split("\\|");
						sb.append("<Variable Name=\"" + varBinding[0] + "\" Value=\"" + varBinding[1] + "\"/>\n");
					}
				}
				sb.append("</Predicate></Node>\n");
			}
			currentIndent = indent;

			currentLine++;
			if (currentLine >= text.size())
				break;
			line = text.get(currentLine);
			indent = countSpaces(line);
		}
		while (!open.isEmpty())
			sb.append("</" + open.pop() + "></Node>\n");
		return currentLine;
	}
}
