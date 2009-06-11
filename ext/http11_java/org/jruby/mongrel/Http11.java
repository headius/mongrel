/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2007 Ola Bini <ola@ologix.com>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.mongrel;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.RubyNumeric;
import org.jruby.RubyObject;
import org.jruby.RubyString;

import org.jruby.runtime.CallbackFactory;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.exceptions.RaiseException;

import org.jruby.util.ByteList;

/**
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a>
 */
public class Http11 extends RubyObject {
    public final static int MAX_FIELD_NAME_LENGTH = 256;
    public final static String MAX_FIELD_NAME_LENGTH_ERR = "HTTP element FIELD_NAME is longer than the 256 allowed length.";
    public final static int MAX_FIELD_VALUE_LENGTH = 80 * 1024;
    public final static String MAX_FIELD_VALUE_LENGTH_ERR = "HTTP element FIELD_VALUE is longer than the 81920 allowed length.";
    public final static int MAX_REQUEST_URI_LENGTH = 1024 * 12;
    public final static String MAX_REQUEST_URI_LENGTH_ERR = "HTTP element REQUEST_URI is longer than the 12288 allowed length.";
    public final static int MAX_FRAGMENT_LENGTH = 1024;
    public final static String MAX_FRAGMENT_LENGTH_ERR = "HTTP element REQUEST_PATH is longer than the 1024 allowed length.";
    public final static int MAX_REQUEST_PATH_LENGTH = 1024;
    public final static String MAX_REQUEST_PATH_LENGTH_ERR = "HTTP element REQUEST_PATH is longer than the 1024 allowed length.";
    public final static int MAX_QUERY_STRING_LENGTH = 1024 * 10;
    public final static String MAX_QUERY_STRING_LENGTH_ERR = "HTTP element QUERY_STRING is longer than the 10240 allowed length.";
    public final static int MAX_HEADER_LENGTH = 1024 * (80 + 32);
    public final static String MAX_HEADER_LENGTH_ERR = "HTTP element HEADER is longer than the 114688 allowed length.";

    // Pre-constructed bytelists to share for all constructed header strings
    public static final ByteList REQUEST_METHOD_BL = ByteList.create("REQUEST_METHOD");
    public static final ByteList REQUEST_URI_BL = ByteList.create("REQUEST_URI");
    public static final ByteList FRAGMENT_BL = ByteList.create("FRAGMENT");
    public static final ByteList REQUEST_PATH_BL = ByteList.create("REQUEST_PATH");
    public static final ByteList QUERY_STRING_BL = ByteList.create("QUERY_STRING");
    public static final ByteList HTTP_VERSION_BL = ByteList.create("HTTP_VERSION");
    public static final ByteList HTTP_CONTENT_LENGTH_BL = ByteList.create("HTTP_CONTENT_LENGTH");
    public static final ByteList CONTENT_LENGTH_BL = ByteList.create("CONTENT_LENGTH");
    public static final ByteList HTTP_CONTENT_TYPE_BL = ByteList.create("HTTP_CONTENT_TYPE");
    public static final ByteList CONTENT_TYPE_BL = ByteList.create("CONTENT_TYPE");
    public static final ByteList GATEWAY_INTERFACE_BL = ByteList.create("GATEWAY_INTERFACE");
    public static final ByteList CGI_1_2_BL = ByteList.create("CGI/1.2");
    public static final ByteList HTTP_HOST_BL = ByteList.create("HTTP_HOST");
    public static final ByteList SERVER_NAME_BL = ByteList.create("SERVER_NAME");
    public static final ByteList SERVER_PORT_BL = ByteList.create("SERVER_PORT");
    public static final ByteList EIGHTY_BL = ByteList.create("80");
    public static final ByteList SERVER_PROTOCOL_BL = ByteList.create("SERVER_PROTOCOL");
    public static final ByteList HTTP_1_1_BL = ByteList.create("HTTP/1.1");
    public static final ByteList SERVER_SOFTWARE_BL = ByteList.create("SERVER_SOFTWARE");
    public static final ByteList MONGREL_1_1_6_BL = ByteList.create("Mongrel 1.1.6");

    public final RubyString REQUEST_METHOD;
    public final RubyString REQUEST_URI;
    public final RubyString FRAGMENT;
    public final RubyString REQUEST_PATH;
    public final RubyString QUERY_STRING;
    public final RubyString HTTP_VERSION;
    public final RubyString HTTP_CONTENT_LENGTH;
    public final RubyString CONTENT_LENGTH;
    public final RubyString HTTP_CONTENT_TYPE;
    public final RubyString CONTENT_TYPE;
    public final RubyString GATEWAY_INTERFACE;
    public final RubyString CGI_1_2;
    public final RubyString HTTP_HOST;
    public final RubyString SERVER_NAME;
    public final RubyString SERVER_PORT;
    public final RubyString EIGHTY;
    public final RubyString SERVER_PROTOCOL;
    public final RubyString HTTP_1_1;
    public final RubyString SERVER_SOFTWARE;
    public final RubyString MONGREL_1_1_6;


    private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new Http11(runtime, klass);
        }
    };

    public static void createHttp11(Ruby runtime) {
        RubyModule mMongrel = runtime.defineModule("Mongrel");
        mMongrel.defineClassUnder("HttpParserError",runtime.getClass("IOError"),runtime.getClass("IOError").getAllocator());

        CallbackFactory cf = runtime.callbackFactory(Http11.class);

        RubyClass cHttpParser = mMongrel.defineClassUnder("HttpParser",runtime.getObject(),ALLOCATOR);
        cHttpParser.defineFastMethod("initialize",cf.getFastMethod("initialize"));
        cHttpParser.defineFastMethod("reset",cf.getFastMethod("reset"));
        cHttpParser.defineFastMethod("finish",cf.getFastMethod("finish"));
        cHttpParser.defineFastMethod("execute",cf.getFastMethod("execute", IRubyObject.class, IRubyObject.class, IRubyObject.class));
        cHttpParser.defineFastMethod("error?",cf.getFastMethod("has_error"));
        cHttpParser.defineFastMethod("finished?",cf.getFastMethod("is_finished"));
        cHttpParser.defineFastMethod("nread",cf.getFastMethod("nread"));
    }

    private Ruby runtime;
    private RubyClass eHttpParserError;
    private Http11Parser hp;

    public Http11(Ruby runtime, RubyClass clazz) {
        super(runtime,clazz);
        this.runtime = runtime;
        this.eHttpParserError = (RubyClass)runtime.getModule("Mongrel").getConstant("HttpParserError");
        this.hp = new Http11Parser();
        this.hp.parser.http_field = http_field;
        this.hp.parser.request_method = request_method;
        this.hp.parser.request_uri = request_uri;
        this.hp.parser.fragment = fragment;
        this.hp.parser.request_path = request_path;
        this.hp.parser.query_string = query_string;
        this.hp.parser.http_version = http_version;
        this.hp.parser.header_done = header_done;
        this.hp.parser.init();

        // prepare all hash keys ahead of time
        REQUEST_METHOD = RubyString.newStringShared(runtime, REQUEST_METHOD_BL);
        REQUEST_METHOD.setFrozen(true);
        REQUEST_URI = RubyString.newStringShared(runtime, REQUEST_URI_BL);
        REQUEST_URI.setFrozen(true);
        FRAGMENT = RubyString.newStringShared(runtime, FRAGMENT_BL);
        FRAGMENT.setFrozen(true);
        REQUEST_PATH = RubyString.newStringShared(runtime, REQUEST_PATH_BL);
        REQUEST_PATH.setFrozen(true);
        QUERY_STRING = RubyString.newStringShared(runtime, QUERY_STRING_BL);
        QUERY_STRING.setFrozen(true);
        HTTP_VERSION = RubyString.newStringShared(runtime, HTTP_VERSION_BL);
        HTTP_VERSION.setFrozen(true);
        HTTP_CONTENT_LENGTH = RubyString.newStringShared(runtime, HTTP_CONTENT_LENGTH_BL);
        HTTP_CONTENT_LENGTH.setFrozen(true);
        CONTENT_LENGTH = RubyString.newStringShared(runtime, CONTENT_LENGTH_BL);
        CONTENT_LENGTH.setFrozen(true);
        HTTP_CONTENT_TYPE = RubyString.newStringShared(runtime, HTTP_CONTENT_TYPE_BL);
        HTTP_CONTENT_TYPE.setFrozen(true);
        CONTENT_TYPE = RubyString.newStringShared(runtime, CONTENT_TYPE_BL);
        CONTENT_TYPE.setFrozen(true);
        GATEWAY_INTERFACE = RubyString.newStringShared(runtime, GATEWAY_INTERFACE_BL);
        GATEWAY_INTERFACE.setFrozen(true);
        CGI_1_2 = RubyString.newStringShared(runtime, CGI_1_2_BL);
        CGI_1_2.setFrozen(true);
        HTTP_HOST = RubyString.newStringShared(runtime, HTTP_HOST_BL);
        HTTP_HOST.setFrozen(true);
        SERVER_NAME = RubyString.newStringShared(runtime, SERVER_NAME_BL);
        SERVER_NAME.setFrozen(true);
        SERVER_PORT = RubyString.newStringShared(runtime, SERVER_PORT_BL);
        SERVER_PORT.setFrozen(true);
        EIGHTY = RubyString.newStringShared(runtime, EIGHTY_BL);
        EIGHTY.setFrozen(true);
        SERVER_PROTOCOL = RubyString.newStringShared(runtime, SERVER_PROTOCOL_BL);
        SERVER_PROTOCOL.setFrozen(true);
        HTTP_1_1 = RubyString.newStringShared(runtime, HTTP_1_1_BL);
        HTTP_1_1.setFrozen(true);
        SERVER_SOFTWARE = RubyString.newStringShared(runtime, SERVER_SOFTWARE_BL);
        SERVER_SOFTWARE.setFrozen(true);
        MONGREL_1_1_6 = RubyString.newStringShared(runtime, MONGREL_1_1_6_BL);
        MONGREL_1_1_6.setFrozen(true);
    }

    public void validateMaxLength(int len, int max, String msg) {
        if(len>max) {
            throw new RaiseException(runtime, eHttpParserError, msg, true);
        }
    }

    private Http11Parser.FieldCB http_field = new Http11Parser.FieldCB() {
            public void call(Object data, int field, int flen, int value, int vlen) {
                RubyHash req = (RubyHash)data;
                RubyString v,f;
                validateMaxLength(flen, MAX_FIELD_NAME_LENGTH, MAX_FIELD_NAME_LENGTH_ERR);
                validateMaxLength(vlen, MAX_FIELD_VALUE_LENGTH, MAX_FIELD_VALUE_LENGTH_ERR);

                v = RubyString.newString(runtime, new ByteList(Http11.this.hp.parser.buffer,value,vlen));
                // FIXME: There should be a way to preallocate these headers
                f = RubyString.newString(runtime, "HTTP_");
                ByteList b = new ByteList(Http11.this.hp.parser.buffer,field,flen);
                for(int i=0,j=b.realSize;i<j;i++) {
                    if((b.bytes[i]&0xFF) == '-') {
                        b.bytes[i] = (byte)'_';
                    } else {
                        b.bytes[i] = (byte)Character.toUpperCase((char)b.bytes[i]);
                    }
                }
                f.cat(b);
                f.setFrozen(true);
                req.fastASet(f,v);
            }
        };

    private Http11Parser.ElementCB request_method = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                RubyString val = RubyString.newString(runtime,new ByteList(hp.parser.buffer,at,length));
                req.fastASet(REQUEST_METHOD,val);
            }
        };

    private Http11Parser.ElementCB request_uri = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                validateMaxLength(length, MAX_REQUEST_URI_LENGTH, MAX_REQUEST_URI_LENGTH_ERR);
                RubyString val = RubyString.newString(runtime,new ByteList(hp.parser.buffer,at,length));
                req.fastASet(REQUEST_URI,val);
            }
        };

    private Http11Parser.ElementCB fragment = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                validateMaxLength(length, MAX_FRAGMENT_LENGTH, MAX_FRAGMENT_LENGTH_ERR);
                RubyString val = RubyString.newString(runtime,new ByteList(hp.parser.buffer,at,length));
                req.fastASet(FRAGMENT,val);
            }
        };

    private Http11Parser.ElementCB request_path = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                validateMaxLength(length, MAX_REQUEST_PATH_LENGTH, MAX_REQUEST_PATH_LENGTH_ERR);
                RubyString val = RubyString.newString(runtime,new ByteList(hp.parser.buffer,at,length));
                req.fastASet(REQUEST_PATH,val);
            }
        };

    private Http11Parser.ElementCB query_string = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                validateMaxLength(length, MAX_QUERY_STRING_LENGTH, MAX_QUERY_STRING_LENGTH_ERR);
                RubyString val = RubyString.newString(runtime,new ByteList(hp.parser.buffer,at,length));
                req.fastASet(QUERY_STRING,val);
            }
        };

    private Http11Parser.ElementCB http_version = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                RubyString val = RubyString.newString(runtime,new ByteList(hp.parser.buffer,at,length));
                req.fastASet(HTTP_VERSION,val);
            }
        };

    private Http11Parser.ElementCB header_done = new Http11Parser.ElementCB() {
            public void call(Object data, int at, int length) {
                RubyHash req = (RubyHash)data;
                IRubyObject temp,ctype,clen;
                
                clen = req.fastARef(HTTP_CONTENT_LENGTH);
                if(clen != null) {
                    req.fastASet(CONTENT_LENGTH,clen);
                }

                ctype = req.fastARef(HTTP_CONTENT_TYPE);
                if(ctype != null) {
                    req.fastASet(CONTENT_TYPE,ctype);
                }

                req.fastASet(GATEWAY_INTERFACE,CGI_1_2);
                if((temp = req.fastARef(HTTP_HOST)) != null) {
                    String s = temp.toString();
                    int colon = s.indexOf(':');
                    if(colon != -1) {
                        req.fastASet(SERVER_NAME,runtime.newString(s.substring(0,colon)));
                        req.fastASet(SERVER_PORT,runtime.newString(s.substring(colon+1)));
                    } else {
                        req.fastASet(SERVER_NAME,temp);
                        req.fastASet(SERVER_PORT,EIGHTY);
                    }
                }

                req.setInstanceVariable("@http_body", RubyString.newString(runtime, new ByteList(hp.parser.buffer, at, length)));
                req.fastASet(SERVER_PROTOCOL,HTTP_1_1);
                req.fastASet(SERVER_SOFTWARE,MONGREL_1_1_6);
            }
        };

    public IRubyObject initialize() {
        this.hp.parser.init();
        return this;
    }

    public IRubyObject reset() {
        this.hp.parser.init();
        return runtime.getNil();
    }

    public IRubyObject finish() {
        this.hp.finish();
        return this.hp.is_finished() ? runtime.getTrue() : runtime.getFalse();
    }

    public IRubyObject execute(IRubyObject req_hash, IRubyObject data, IRubyObject start) {
        int from = 0;
        from = RubyNumeric.fix2int(start);
        ByteList d = ((RubyString)data).getByteList();
        if(from >= d.realSize) {
            throw new RaiseException(runtime, eHttpParserError, "Requested start is after data buffer end.", true);
        } else {
            this.hp.parser.data = req_hash;
            this.hp.execute(d,from);
            validateMaxLength(this.hp.parser.nread,MAX_HEADER_LENGTH, MAX_HEADER_LENGTH_ERR);
            if(this.hp.has_error()) {
                throw new RaiseException(runtime, eHttpParserError, "Invalid HTTP format, parsing fails.", true);
            } else {
                return runtime.newFixnum((long)this.hp.parser.nread);
            }
        }
    }

    public IRubyObject has_error() {
        return this.hp.has_error() ? runtime.getTrue() : runtime.getFalse();
    }

    public IRubyObject is_finished() {
        return this.hp.is_finished() ? runtime.getTrue() : runtime.getFalse();
    }

    public IRubyObject nread() {
        return runtime.newFixnum(this.hp.parser.nread);
    }
}// Http11
