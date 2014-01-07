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
/*                        JSONArrayReader                         */
/*================================================================*/

org.webpki.json.JSONArrayReader = function (/* Vector<org.webpki.json.JSONValue> */array)
{
    this.index = 0;
    this.array = array;
};

/* public boolean */org.webpki.json.JSONArrayReader.prototype.hasMore = function ()
{
    return this.index < this.array.length;
};

/* void */org.webpki.json.JSONArrayReader.prototype._inRangeCheck = function ()
{
    if (!this.hasMore ())
    {
        org.webpki.json.JSONError._error ("Trying to read past of array limit: " + this.index);
    }
};

/* Object */org.webpki.json.JSONArrayReader.prototype._get = function (/* org.webpki.json.JSONTypes */expected_type)
{
    this._inRangeCheck ();
    /* org.webpki.json.JSONValue */var value = this.array[this.index++];
    if (!expected_type.isCompatible (value.type))
    {
        org.webpki.json.JSONError._error ("Incompatible request: " +
                "Read=" + org.webpki.json.JSONValue.prototype.getJSONTypeName (value.type) +
                ", Expected=" + org.webpki.json.JSONValue.prototype.getJSONTypeName (expected_type));
    }
    return value.value;
};

/* public String */org.webpki.json.JSONArrayReader.prototype.getString = function ()
{
    return /* (String) */this._get (org.webpki.json.JSONTypes.STRING);
};

/* public int */org.webpki.json.JSONArrayReader.prototype.getInt = function ()
{
    return parseInt (/* (String) */this._get (org.webpki.json.JSONTypes.INTEGER));
};

/* public long */org.webpki.json.JSONArrayReader.prototype.getLong = function ()
{
    return this._get (org.webpki.json.JSONTypes.INTEGER);
};

/* public BigInteger */org.webpki.json.JSONArrayReader.prototype.getBigInteger = function ()
{
    return this._get (org.webpki.json.JSONTypes.INTEGER);
};

/* public BigDecimal */org.webpki.json.JSONArrayReader.prototype.getBigDecimal = function ()
{
    return new this, _get (org.webpki.json.JSONTypes.DECIMAL);
};
/*
 public GregorianCalendar getDateTime () throws IOException
 {
 return ISODateTime.parseDateTime (getString ());
 }

 public double getDouble () throws IOException
 {
 return new Double ((String) get (org.webpki.json.JSONTypes.DOUBLE));
 }

 public boolean getBoolean () throws IOException
 {
 return new Boolean ((String) get (org.webpki.json.JSONTypes.BOOLEAN));
 }

 public boolean getIfNULL () throws IOException
 {
 if (getElementType () == org.webpki.json.JSONTypes.NULL)
 {
 scanAway ();
 return true;
 }
 return false;
 }
 */
/* public org.webpki.json.JSONArrayReader */org.webpki.json.JSONArrayReader.prototype.getArray = function ()
{
    return new org.webpki.json.JSONArrayReader (/* (Vector<org.webpki.json.JSONValue>) */this._get (org.webpki.json.JSONTypes.ARRAY));
};
/*
 public org.webpki.json.JSONTypes getElementType () throws IOException
 {
 _inRangeCheck ();
 return array.elementAt (index).type;
 }
 */
/* public org.webpki.json.JSONObjectReader */org.webpki.json.JSONArrayReader.prototype.getObject = function ()
{
    return new org.webpki.json.JSONObjectReader (/* (org.webpki.json.JSONObject) */this._get (org.webpki.json.JSONTypes.OBJECT));
};
/*
 public void scanAway () throws IOException
 {
 get (getElementType ());
 }
 }
 */
