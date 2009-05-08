// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.common;

import com.google.common.collect.Lists;
import com.google.parser.Callback;
import com.google.parser.Chset;
import com.google.parser.Parser;
import com.google.parser.Strcaselit;
import com.google.parser.Strlit;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for parsing the Set-Cookie and Set-Cookie2 http header. Parser
 * derived from RFC 2109 (for Set-Cookie) and RFC 2965 (for Set-Cookie2)
 */
public class SetCookieParser {
  private static final Logger logger_ =
      Logger.getLogger(SetCookieParser.class.getName());

  private static final Parser<Result> setCookieParser_;

  private static class Result {
    ArrayList<SetCookie> cookies_;
    SetCookie currentCookie_;
    public Result() {
      this.cookies_ = Lists.newArrayList();
    }
  }

  public static ArrayList<SetCookie> parse(Enumeration<String> enumeration) {
    if ( (enumeration == null) || !enumeration.hasMoreElements() ) {
      return null;
    }

    Result result = new Result();
    while ( enumeration.hasMoreElements() ) {
      String str = enumeration.nextElement();
      try {
        setCookieParser_.parse(str, result);
      } catch (Exception e) {
        result.cookies_.clear();
        logger_.log(Level.WARNING, "parsing Set-Cookie header '" + str + "'", e);
      }
    }
    return result.cookies_;
  }

  public static ArrayList<SetCookie> parse(String str) {
    Result result = new Result();
    try {
      setCookieParser_.parse(str, result);
    } catch (Exception e) {
      result.cookies_.clear();
      logger_.log(Level.WARNING, "parsing Set-Cookie header '" + str + "'", e);
    }
    return result.cookies_;
  }

  static String create(char[] buf, int start, int end) {
    if ( (buf[start] == '\"') && (buf[end - 1] == '\"') ) {
      start += 1;
      end -= 1;
    }
    return new String(buf, start, end - start);
  }

  static class NameAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_ = new SetCookie(create(buf, start, end), "");
      result.cookies_.add(result.currentCookie_);
    }
  }

  static class ValueAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setValue(create(buf, start, end));
    }
  }

  static class CommentAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setComment(create(buf, start, end));
    }
  }

  static class MaxAgeAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      try {
        result.currentCookie_.setMaxAge(
          Integer.parseInt(create(buf, start, end)));
      } catch (NumberFormatException ignored) {}
    }
  }

  static class PathAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setPath(create(buf, start, end));
    }
  }

  static class ExpireAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setExpires(create(buf, start, end));
    }
  }

  static class SecureAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setSecure(true);
    }
  }

  static class HttpOnlyAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setHttpOnly(true);
    }
  }

  static class DiscardAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setDiscard(true);
    }
  }

  static class VersionAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      try {
        result.currentCookie_.setVersion(
          Integer.parseInt(create(buf, start, end)));
      } catch (NumberFormatException ignored) { }
    }
  }

  static class DomainAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      result.currentCookie_.setDomain(create(buf, start, end));
    }
  }

  static class CommentURLAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      if ( buf[start] == '"' && buf[end] == '"' )
        result.currentCookie_.commentURL = create(buf, start+1, end-1);
      else if ( buf[start] != '"' && buf[end] != '"' )
        result.currentCookie_.commentURL = create(buf, start, end);
    }
  }

  static class PortsAction implements Callback<Result> {
    public void handle(char[] buf, int start, int end, Result result) {
      String portStr;
      if ( buf[start] == '"' && buf[end] == '"' )
        portStr = create(buf, start+1, end-1);
      else if ( buf[start] != '"' && buf[end] != '"' )
        portStr = create(buf, start, end);
      else
        return;
      StringTokenizer strtok = new StringTokenizer(portStr, " ,");
      try {
        if ( strtok.countTokens() != 0 ) {
          int[] ports = new int[strtok.countTokens()];
          for ( int i = 0; i < ports.length; i++ )
            ports[i] = Integer.parseInt(strtok.nextToken());
          result.currentCookie_.ports = ports;
        } else {
          result.currentCookie_.ports = SetCookie.EMPTY_PORT;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /** This action is here for debugging only */
  static class DebugAction implements Callback<Object> {
    private String msg;
    public DebugAction(String msg) {
      this.msg = msg;
    }
    public void handle(char[] buf, int start, int end, Object data) {
      String str = null;
      try {
        str = new String (buf, start, end - start);
      } catch (Exception e) {
        str = "error= " + e;
      }
      System.out.println(msg + ": str = [" + str + "]");
    }
  }

  static Parser<Object> makeStrlitChoice(String[] strs) {
    if ( strs.length == 0 )
      return null;
    Parser<Object> parser = new Strlit(strs[strs.length - 1]);
    for ( int i = strs.length - 2; i >= 0; --i ) {
      parser = Parser.alternative(new Strlit(strs[i]), parser);
    }
    return parser;
  }


/*
 * makeHttpDateParser():
 * This parser handle both types of Date string, by RFC 1123 and RFC 850
 *
 * The below grammar was obtained from
 *          http://www.freesoft.org/CIE/RFC/1945/14.htm
 *
 *    HTTP-date      = rfc1123-date | rfc850-date | asctime-date
 *
 *      rfc1123-date   = wkday "," SP date1_flexible SP time SP "GMT"
 *      rfc850-date    = weekday "," SP date2_flexible SP time SP "GMT"
 *      asctime-date   = wkday SP date3 SP time SP 4DIGIT
 *
 *      date1_flexible = date1 | date1_alt
 *      date1          = 2DIGIT SP month SP 4DIGIT
 *                       ; day month year (e.g., 02 Jun 1982)
 *      date1_alt      = 2DIGIT "-" month "-" 4DIGIT
 *                       ; day-month-year (e.g., 02-Jun-1982)
 *      date2_flexible = date2 | date2_alt
 *      date2          = 2DIGIT "-" month "-" 2DIGIT
 *                       ; day-month-year (e.g., 02-Jun-82)
 *      date2_alt      = 2DIGIT SP month SP 2DIGIT
 *                       ; day month year (e.g., 02 Jun 82)
 *      date3          = month SP ( 2DIGIT | ( SP 1DIGIT ))
 *                       ; month day (e.g., Jun  2)
 *
 *     time           = 2DIGIT ":" 2DIGIT ":" 2DIGIT
 *                      ; 00:00:00 - 23:59:59
 *
 *     wkday          = "Mon" | "Tue" | "Wed"
 *                    | "Thu" | "Fri" | "Sat" | "Sun"
 *
 *     weekday        = "Monday" | "Tuesday" | "Wednesday"
 *                    | "Thursday" | "Friday" | "Saturday" | "Sunday"
 *
 *     month          = "Jan" | "Feb" | "Mar" | "Apr"
 *                    | "May" | "Jun" | "Jul" | "Aug"
 *                    | "Sep" | "Oct" | "Nov" | "Dec"
 *
 *
 * Example:
 *   Sun, 06 Nov 1994 08:49:37 GMT    ; RFC 822, updated by RFC 1123
 *   Sunday, 06-Nov-94 08:49:37 GMT   ; RFC 850, obsoleted by RFC 1036
 *   Sun Nov  6 08:49:37 1994         ; ANSI C's asctime() format
 *   Thu, 01-Jan-1970 00:00:10 GMT    ; Nonstandard but seen at customer site.
 */
  static Parser<Result> makeHttpDateParser(Callback<Result> action) {
    String[] wkdayStrs = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    String[] weekdayStrs = {"Monday", "Tuesday", "Wednesday", "Thursday",
                            "Friday", "Saturday", "Sunday"};
    String[] monthStrs = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    Parser<Object> wkday =  makeStrlitChoice(wkdayStrs);
    Parser<Object> weekday = makeStrlitChoice(weekdayStrs);
    Parser<Object> month = makeStrlitChoice(monthStrs);
    Parser<Object> two_digits = Chset.DIGIT.repeat(2);
    Parser<Object> four_digits = Chset.DIGIT.repeat(4);
    Parser<Object> space = new Chset(' ');
    Parser<Object> dash = new Chset('-');

    Parser<Object> date1 = Parser.
      sequence(Parser.sequence(two_digits, space),
               Parser.sequence(month, Parser.sequence(space, four_digits)));
    Parser<Object> date1Alt = Parser.
      sequence(Parser.sequence(two_digits, dash),
               Parser.sequence(month, Parser.sequence(dash, four_digits)));
    Parser<Object> date1Flexible = Parser.alternative(date1, date1Alt);

    Parser<Object> date2 = Parser.
      sequence(Parser.sequence(two_digits, dash),
               Parser.sequence(month, Parser.sequence(dash, two_digits)));

    Parser<Object> date2Alt = Parser.
      sequence(Parser.sequence(two_digits, space),
               Parser.sequence(month, Parser.sequence(space, two_digits)));
    Parser<Object> date2Flexible = Parser.alternative(date2, date2Alt);

    Parser<Object> minute_second = Parser.sequence(new Chset("0-5"),
                                                   Chset.DIGIT);
    Parser<Object> hour  = Parser.alternative(
      Parser.sequence(new Chset("0-1"), Chset.DIGIT),
      Parser.sequence(new Chset('2'), new Chset("0-3")));
    Parser<Object> time  = Parser.
      sequence(hour, Parser.sequence(new Chset(':'), minute_second).repeat(2));

    Parser<Object> rfc1123_date = Parser.
      sequence(Parser.sequence(wkday, new Strlit(", ")),
               Parser.sequence(date1Flexible, space));
    rfc1123_date = Parser.sequence(
      Parser.sequence(rfc1123_date, time),
      Parser.sequence(space, new Strlit("GMT")));

    Parser<Object> rfc850_date =  Parser.
      sequence(Parser.sequence(weekday, new Strlit(", ")),
               Parser.sequence(date2Flexible, space));
    rfc850_date = Parser.sequence(
      Parser.sequence(rfc850_date, time),
      Parser.sequence(space, new Strlit("GMT")));

    Parser<Result> date = Parser.alternative(rfc1123_date, rfc850_date);
    if (action != null)
      date = date.action(action);

    // accept wrapping double-quotes
    date = Parser.
      alternative(date, Parser.
                  sequence(new Chset('"'),
                           Parser.sequence(date, new Chset('"'))));
    return date;
  }

  /* makeAVParser():
   * This Parser matches a name-value pair.
   */
  static Parser<Result> makeAVParser(String name, Callback<Result> callback) {
    Chset wsp = Chset.WHITESPACE;
    Parser<Result> value_n_action = Parser.alternative(
      Parser.sequence(new Chset('"'),
                      Parser.sequence(Chset.not(new Chset('"')).
                                      star().action(callback),
                                      new Chset('"'))),
      Chset.not(Chset.union(wsp, new Chset(";,"))).plus().action(callback) );

    Parser<Object> avParser = new Strcaselit(name);
    avParser = Parser.sequence(wsp.star(), avParser);
    avParser = Parser.sequence(avParser, wsp.star());
    avParser = Parser.sequence(avParser, new Chset('='));
    avParser = Parser.sequence(avParser, wsp.star());

    return Parser.sequence(avParser, value_n_action.optional());
  }

 /** SetCookieParser:
  *****************************************
  * From RFC 2109:
  *
  *
  *   set-cookie      =       "Set-Cookie:" cookies
  *   cookies         =       1#cookie
  *   cookie          =       NAME "=" VALUE *(";" set-cookie-av)
  *   NAME            =       attr
  *   VALUE           =       value
  *   set-cookie-av   =       "Comment" "=" value
  *                   |       "Domain" "=" value
  *                   |       "Max-Age" "=" value
  *                   |       "Path" "=" value
  *                   |       "Secure"
  *                   |       "Version" "=" 1*DIGIT
  ************************************
  *
  * RFC 2965:
  *   set-cookie      =       "Set-Cookie2:" cookies
  *   cookies         =       1#cookie
  *   cookie          =       NAME "=" VALUE *(";" set-cookie-av)
  *   NAME            =       attr
  *   VALUE           =       value
  *   set-cookie-av   =       "Comment" "=" value
  *                   |       "CommentURL" "=" <"> http_URL <">
  *                   |       "Discard"
  *                   |       "Domain" "=" value
  *                   |       "Max-Age" "=" value  (RFC2616)
  *                   |       "Path" "=" value
  *                   |       "Port" [ "=" <"> portlist <"> ]
  *                   |       "Secure"
  *                   |       "Version" "=" 1*DIGIT
  *   portlist        =       1#portnum
  *   portnum         =       1*DIGIT
  ************************************
  * We also handle a new field supported by MS ASP.NET / IE6
  * set-cookie-av     =       "HttpOnly"
  *
  */
  static {
    Chset wsp = Chset.WHITESPACE;
    Chset name_token = Chset.not(Chset.union(wsp, new Chset("=;,")));

    Parser<Object> value_sep = Parser.sequence(wsp.star(), new Chset(";"));
    value_sep = Parser.sequence(value_sep, wsp.star());

    Parser<Object> cookie_sep = Parser.sequence(wsp.star(), new Chset(","));
    cookie_sep = Parser.sequence(cookie_sep, wsp.star());

    Parser<Result> comment = makeAVParser("comment", new CommentAction());
    Parser<Result> domain  = makeAVParser("domain",  new DomainAction());
    Parser<Result> maxage  = makeAVParser("max-age", new MaxAgeAction());
    Parser<Result> path    = makeAVParser("path", new PathAction());
    Parser<Result> version = makeAVParser("version", new VersionAction());
    @SuppressWarnings("unused")
    Parser<Result> ports   = makeAVParser("ports", new PortsAction());
    @SuppressWarnings("unused")
    Parser<Result> commentURL =
      makeAVParser("commentURL", new CommentURLAction());

    Parser<Result> secure = Parser.sequence(wsp.star(),
                                            new Strcaselit("secure"));
    secure = Parser.sequence(secure, wsp.star()).action(new SecureAction());

    Parser<Result> httponly = Parser.sequence(wsp.star(),
                                              new Strcaselit("httponly"));
    httponly = Parser.sequence(httponly, wsp.star()).action(
        new HttpOnlyAction());

    Parser<Result> discard = Parser.sequence(wsp.star(),
                                             new Strcaselit("discard"));
    discard = Parser.sequence(discard, wsp.star()).action(new DiscardAction());

    Parser<Result> expires = Parser.sequence(wsp.star(),
                                             new Strcaselit("expires"));
    expires = Parser.sequence(expires, wsp.star());
    expires = Parser.sequence(expires, new Chset('='));
    expires = Parser.sequence(expires, wsp.star());
    expires = Parser.sequence(expires, makeHttpDateParser(new ExpireAction()));

    Parser<Result> cookie_av;
    cookie_av = Parser.alternative(comment, domain);
    cookie_av = Parser.alternative(maxage, cookie_av);
    cookie_av = Parser.alternative(path, cookie_av);
    cookie_av = Parser.alternative(expires, cookie_av);
    cookie_av = Parser.alternative(secure, cookie_av);
    cookie_av = Parser.alternative(httponly, cookie_av);
    cookie_av = Parser.alternative(discard, cookie_av);
    cookie_av = Parser.alternative(version, cookie_av);

    cookie_av = cookie_av.list(value_sep);
    cookie_av = Parser.sequence(value_sep, cookie_av);

    Parser<Result> name = name_token.plus().action(new NameAction());
    Parser<Result> value = Parser.alternative(
      Parser.sequence(new Chset('"'),
                      Parser.sequence(Chset.not(new Chset('"')).star().
                                      action(new ValueAction()),
                                      new Chset('"'))),
      Chset.not(Chset.union(wsp, new Chset(",;"))).plus().
      action(new ValueAction()));

    value = Parser.sequence(value.optional(), cookie_av.optional());
    value = Parser.sequence(wsp.star(), value);
    value = Parser.sequence(new Chset('='), value);

    Parser<Result> cookie = Parser.sequence(name, wsp.star());
    cookie = Parser.sequence(cookie, value);

    setCookieParser_ = cookie.list(cookie_sep);
  }

}
