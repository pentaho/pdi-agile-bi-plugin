/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.agilebi.spoon.util;

import java.io.UnsupportedEncodingException;
import java.net.CookiePolicy;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class EncodingUtil {


  private static final List<Character> reservedChars = Collections.unmodifiableList(
    Arrays.asList( new Character[] { '/', ':', '[', ']', '*', '\'', '"', '|', '\t', '\r', '\n' } ) );

  private static final String reservedCharStr = "/, :, [, ], *, \\, \", |,  TAB, CR, LF";

  private static final Pattern containsReservedCharsPattern = makePattern( getReservedChars() );

  private static Pattern makePattern( List<Character> list) {
    // escape all reserved characters as they may have special meaning to regex engine
    StringBuilder buf = new StringBuilder();
    buf.append( ".*[" ); //$NON-NLS-1$
    for ( Character ch : list ) {
      buf.append( "\\" ); //$NON-NLS-1$
      buf.append( ch );
    }
    buf.append( "]+.*" ); //$NON-NLS-1$
    return Pattern.compile( buf.toString() );
  }

  /**
   * Checks for presence of black listed chars as well as illegal permutations of legal chars.
   */
  public static boolean isValidString(final String s) {
    return s != null && s.trim().length() > 0 &&
      s.trim().equals( s ) && // no leading or trailing whitespace
      !containsReservedCharsPattern.matcher( s ).matches() && // no reserved characters
      !".".equals( s ) && // no . //$NON-NLS-1$
      !"..".equals( s ) ;  // no .. //$NON-NLS-1$
  }

  /**
   * string encoding is centralized at this method
   * @param s string to encode
   * @return encoded string using UTF-8
   */
  public static String encodeUTF8( String s ) throws UnsupportedEncodingException {

    if( s != null ) {

      s = URLEncoder.encode( s , "UTF-8" ); //$NON-NLS-1$

      // additional char encoding may be performed here
    }

    return s;
  }

  /**
   * string decoding is centralized at this method
   * @param s string to decode
   * @return decoded string using UTF-8
   */
  public static String decodeUTF8( String s )  throws UnsupportedEncodingException {

    if( s != null ) {

      s = URLDecoder.decode( s, "UTF-8" ); //$NON-NLS-1$

      // additional char decoding may be performed here
    }

    return s;
  }

  public static List<Character> getReservedChars() {
    return reservedChars;
  }

  public static String getReservedCharStr() {
    return reservedCharStr;
  }
}
