/*
 *  Copyright 2006-2014 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/*================================================================*/
/*                           JSONParser                           */
/*================================================================*/

org.webpki.json.JSONParser = function ()
{
    this.LEFT_CURLY_BRACKET  = '{';
    this.RIGHT_CURLY_BRACKET = '}';
    this.BLANK_CHARACTER     = ' ';
    this.DOUBLE_QUOTE        = '"';
    this.COLON_CHARACTER     = ':';
    this.LEFT_BRACKET        = '[';
    this.RIGHT_BRACKET       = ']';
    this.COMMA_CHARACTER     = ',';
    this.BACK_SLASH          = '\\';

    this.INTEGER_PATTERN         = new RegExp ("^((0)|(-?[1-9][0-9]*))$");
    this.BOOLEAN_PATTERN         = new RegExp ("^(true|false)$");
    this.DECIMAL_INITIAL_PATTERN = new RegExp ("^((\\+|-)?[0-9]+[\\.][0-9]+)$");
    this.DECIMAL_2DOUBLE_PATTERN = new RegExp ("^((\\+.*)|([-][0]*[\\.][0]*))$");
    this.DOUBLE_PATTERN          = new RegExp ("^([-+]?(([0-9]*\\.?[0-9]+)|([0-9]+\\.?[0-9]*))([eE][-+]?[0-9]+)?)$");
};

/* org.webpki.json.JSONObjectReader */org.webpki.json.JSONParser.prototype.parse = function (/* String */json_string)
{
    this.json_data = json_string;
    this.max_length = json_string.length;
    this.index = 0;
    var root = new org.webpki.json.JSONObject ();
    if (this._testNextNonWhiteSpaceChar () == this.LEFT_BRACKET)
    {
        this._scan ();
        root._setArray (this._scanArray ("outer array"));
    }
    else
    {
        this._scanFor (this.LEFT_CURLY_BRACKET);
        this._scanObject (root);
    }
    while (this.index < this.max_length)
    {
        if (!this._isWhiteSpace (this.json_data.charAt (this.index++)))
        {
            org.webpki.json.JSONError._error ("Improperly terminated JSON object");
        }
    }
    return new org.webpki.json.JSONObjectReader (root);
};

/* String */org.webpki.json.JSONParser.prototype._scanProperty = function ()
{
    this._scanFor (this.DOUBLE_QUOTE);
    var property = this._scanQuotedString ().value;
    if (property.length == 0)
    {
        org.webpki.json.JSONError._error ("Empty property");
    }
    this._scanFor (this.COLON_CHARACTER);
    return property;
};

/* org.webpki.json.JSONValue */org.webpki.json.JSONParser.prototype._scanObject = function (/* org.webpki.json.JSONObject */holder)
{
    /* boolean */var next = false;
    while (this._testNextNonWhiteSpaceChar () != this.RIGHT_CURLY_BRACKET)
    {
        if (next)
        {
            this._scanFor (this.COMMA_CHARACTER);
        }
        next = true;
        /* String */var name = this._scanProperty ();
        /* org.webpki.json.JSONValue */var value;
        switch (this._scan ())
        {
            case this.LEFT_CURLY_BRACKET:
                value = this._scanObject (new org.webpki.json.JSONObject ());
                break;

            case this.DOUBLE_QUOTE:
                value = this._scanQuotedString ();
                break;

            case this.LEFT_BRACKET:
                value = this._scanArray (name);
                break;

            default:
                value = this._scanSimpleType ();
        }
        holder._addProperty (name, value);
    }
    this._scan ();
    return new org.webpki.json.JSONValue (org.webpki.json.JSONTypes.OBJECT, holder);
};

/* org.webpki.json.JSONValue */org.webpki.json.JSONParser.prototype._scanArray = function (/* String */name)
{
    var arr_index = 0;
    /* Vector<org.webpki.json.JSONValue> */var array = [] /* new Vector<org.webpki.json.JSONValue> () */;
    /* org.webpki.json.JSONValue */var value = null;
    /* boolean */var next = false;
    while (this._testNextNonWhiteSpaceChar () != this.RIGHT_BRACKET)
    {
        if (next)
        {
            this._scanFor (this.COMMA_CHARACTER);
        }
        else
        {
            next = true;
        }
        switch (this._scan ())
        {
            case this.LEFT_BRACKET:
                value = this._scanArray (name);
                break;

            case this.LEFT_CURLY_BRACKET:
                value = this._scanObject (new org.webpki.json.JSONObject ());
                break;

            case this.DOUBLE_QUOTE:
                value = this._scanQuotedString ();
                break;

            default:
                value = this._scanSimpleType ();
        }
        array[arr_index++] = value;
    }
    this._scan ();
    return new org.webpki.json.JSONValue (org.webpki.json.JSONTypes.ARRAY, array);
};

/* org.webpki.json.JSONValue */org.webpki.json.JSONParser.prototype._scanSimpleType = function ()
{
    this.index--;
    /* StringBuffer */var result = new String () /* StringBuffer () */;
    /* char */var c;
    while ((c = this._testNextNonWhiteSpaceChar ()) != this.COMMA_CHARACTER && c != this.RIGHT_BRACKET && c != this.RIGHT_CURLY_BRACKET)
    {
        if (this._isWhiteSpace (c = this._nextChar ()))
        {
            break;
        }
        result += c;
    }
    if (result.length == 0)
    {
        org.webpki.json.JSONError._error ("Missing argument");
    }
    /* org.webpki.json.JSONTypes */var type = org.webpki.json.JSONTypes.INTEGER;
    if (!this.INTEGER_PATTERN.test (result))
    {
        if (this.BOOLEAN_PATTERN.test (result))
        {
            type = org.webpki.json.JSONTypes.BOOLEAN;
        }
        else if (result == "null")
        {
            type = org.webpki.json.JSONTypes.NULL;
        }
        else if (this.DECIMAL_INITIAL_PATTERN.test (result))
        {
            type = this.DECIMAL_2DOUBLE_PATTERN.test (result) ? org.webpki.json.JSONTypes.DOUBLE : org.webpki.json.JSONTypes.DECIMAL;
        }
        else
        {
            type = org.webpki.json.JSONTypes.DOUBLE;
            if (!this.DOUBLE_PATTERN.test (result))
            {
                org.webpki.json.JSONError._error ("Undecodable argument: " + result);
            }
        }
    }
    return new org.webpki.json.JSONValue (type, result);
};

/* org.webpki.json.JSONValue */org.webpki.json.JSONParser.prototype._scanQuotedString = function ()
{
    /* StringBuffer */var result = new String () /* StringBuffer () */;
    while (true)
    {
        /* char */var c = this._nextChar ();
        if (c < ' ')
        {
            org.webpki.json.JSONError._error ("Unescaped control character: " + c);
        }
        if (c == this.DOUBLE_QUOTE)
        {
            break;
        }
        if (c == this.BACK_SLASH)
        {
            switch (c = this._nextChar ())
            {
                case '"':
                case '\\':
                case '/':
                    break;

                case 'b':
                    c = '\b';
                    break;

                case 'f':
                    c = '\f';
                    break;

                case 'n':
                    c = '\n';
                    break;

                case 'r':
                    c = '\r';
                    break;

                case 't':
                    c = '\t';
                    break;

                case 'u':
                    var unicode_char = 0;
                    for (var i = 0; i < 4; i++)
                    {
                        unicode_char = ((unicode_char << 4) + this._getHexChar ());
                    }
                    c = String.fromCharCode (unicode_char);
                    break;

                default:
                    org.webpki.json.JSONError._error ("Unsupported escape:" + c);
            }
        }
        result += c;
    }
    return new org.webpki.json.JSONValue (org.webpki.json.JSONTypes.STRING, result);
};

/* int */org.webpki.json.JSONParser.prototype._getHexChar = function ()
{
    /* char */var c = this._nextChar ();
    switch (c)
    {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return c.charCodeAt (0) - 48;

        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
            return c.charCodeAt (0) - 87;

        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
            return c.charCodeAt (0) - 55;
    }
    org.webpki.json.JSONError._error ("Bad hex in \\u escape: " + c);
};

/* boolean */org.webpki.json.JSONParser.prototype._isNumber = function (/* char */c)
{
    return c >= '0' && c <= '9';
};

/* char */org.webpki.json.JSONParser.prototype._testNextNonWhiteSpaceChar = function ()
{
    /* int */var save = this.index;
    /* char */var c = this._scan ();
    this.index = save;
    return c;
};

/* void */org.webpki.json.JSONParser.prototype._scanFor = function (/* char */expected)
{
    /* char */var c = this._scan ();
    if (c != expected)
    {
        org.webpki.json.JSONError._error ("Expected '" + expected + "' but got '" + c + "'");
    }
};

/* char */org.webpki.json.JSONParser.prototype._nextChar = function ()
{
    if (this.index < this.max_length)
    {
        return this.json_data.charAt (this.index++);
    }
    org.webpki.json.JSONError._error ("Unexpected EOF reached");
};

/* boolean */org.webpki.json.JSONParser.prototype._isWhiteSpace = function (/* char */c)
{
    return c <= this.BLANK_CHARACTER;
};

/* char */org.webpki.json.JSONParser.prototype._scan = function ()
{
    while (true)
    {
        /* char */var c = this._nextChar ();
        if (this._isWhiteSpace (c))
        {
            continue;
        }
        return c;
    }
};
