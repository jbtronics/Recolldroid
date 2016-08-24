/*
 * (c) 2013 Martin van Zuilekom (http://martin.cubeactive.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.inscription;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

/*
 * Class to show a credits dialog
 *
 * Example xml
	<credits>
	    <section title="Development">
		    <function title="Created by">
		        <credit>Martin van Zuilekom</credit>        
			</function>	    	    	   
		    <function title="Code contribution">
		        <credit>Hameno</credit>        
		        <credit>Pepyakin</credit>        
			</function>	    	    	   
		</section>
		<websites>
		    <url title="Inscription on Github">https://github.com/MartinvanZ/Inscription</url>
		</websites>	
		<copyright>(c) 2013 Martin van Zuilekom. Licensed under the Apache License, Version 2.0 (the "License").<url>http://www.apache.org/licenses/LICENSE-2.0</url></copyright>
	</credits> 
 */

public class CreditsDialog {
    static final private String TAG = "CreditsDialog"; 
    
//    static final private String TITLE_CREDITS = "title_credits"; 
    static final private String CREDITS_XML = "credits"; 

    private final Context mContext;

    private String mStyle =   "body { font-size: 9pt; text-align: center; }" 
			+ "h1 { margin-top: 20px; margin-bottom: 15px; margin-left: 0px; font-size: 1.7EM; text-align: center; }" 
			+ "h2 { margin-top: 15px; margin-bottom: 5px; padding-left: 0px; margin-left: 0px; font-size: 1EM; }" 
			+ "li { margin-left: 0px; font-size: 1EM; }" 
			+ "ul { margin-top: 0px;   margin-bottom: 5px; padding-left: 0px; list-style-type: none; }"
			+ "a { color: #777777; }"				
			+ "</style>";

    private int mIcon = 0;

	private int mTitle = R.string.title_credits;
    
	public CreditsDialog(final Context context) {
        mContext = context;
    }


	//Parse the copyright tag and return html code
	private String parseCreditTag(final XmlResourceParser aXml) throws XmlPullParserException, IOException {
		String _Result = "";
        int eventType = aXml.getEventType();
        while (!((eventType == XmlPullParser.END_TAG) && (aXml.getName().equals("credit")))) {
        	if (eventType == XmlPullParser.TEXT){
        		_Result = _Result + aXml.getText();
            }
        	if ((eventType == XmlPullParser.START_TAG) && (aXml.getName().equals("url"))){
        		_Result = _Result + parseUrlTag(aXml);
            }
        	eventType = aXml.next();
        }		
        return _Result;
	}
	
	//Parse a function tag and return html code
	private String parseFunctionTag(final XmlResourceParser aXml) throws XmlPullParserException, IOException {
		String _Result = "<h2>" + aXml.getAttributeValue(null, "title") + "</h2><ul>";
        int eventType = aXml.getEventType();
        while ((eventType != XmlPullParser.END_TAG) || (aXml.getName().equals("credit"))) {
        	if ((eventType == XmlPullParser.START_TAG) && (aXml.getName().equals("credit"))){
            	eventType = aXml.next();
        		_Result = _Result + "<li>" + parseCreditTag(aXml) + "</li>";
            }
        	eventType = aXml.next();
        }		
        _Result = _Result + "</ul>";
        return _Result;
	}
	
	//Parse a section tag and return html code
	private String parseSectionTag(final XmlResourceParser aXml) throws XmlPullParserException, IOException {
		String _Result = "<h1>" + aXml.getAttributeValue(null, "title") + "</h1>";
        int eventType = aXml.getEventType();
        while ((eventType != XmlPullParser.END_TAG) || (aXml.getName().equals("function"))) {
        	if ((eventType == XmlPullParser.START_TAG) && (aXml.getName().equals("function"))){
        		_Result = _Result + parseFunctionTag(aXml);
            }
        	eventType = aXml.next();
        }		
        return _Result;
	}

	//Parse the url tag and return html code
	private String parseUrlTag(final XmlResourceParser aXml) throws XmlPullParserException, IOException {
        int eventType = aXml.getEventType();
		String _Url = "";
		String _Title = "";
		if (aXml.getAttributeValue(null, "title") != null)
			_Title = aXml.getAttributeValue(null, "title");
        while (eventType != XmlPullParser.END_TAG) {
        	if (eventType == XmlPullParser.TEXT){
        		_Url = aXml.getText();
            }
        	eventType = aXml.next();
        }		
		if (_Url.equals(""))
			return "";
		if (_Title.equals(""))
			_Title = _Url;
		return String.format("<br /><a href ='%1$s'>%2$s</a>", _Url, _Title);		
	}
	
	//Parse the copyright tag and return html code
	private String parseCopyrightTag(final XmlResourceParser aXml) throws XmlPullParserException, IOException {
		String _Result = "<br /><br />";
        int eventType = aXml.getEventType();
        while (!((eventType == XmlPullParser.END_TAG) && (aXml.getName().equals("copyright")))) {
        	if (eventType == XmlPullParser.TEXT){
        		_Result = _Result + aXml.getText();
            }
        	if ((eventType == XmlPullParser.START_TAG) && (aXml.getName().equals("url"))){
        		_Result = _Result + parseUrlTag(aXml);
            }
        	eventType = aXml.next();
        }		
        return _Result;
	}
	
	//Parse the websites tag and return html code
	private String parseWebsitesTag(final XmlResourceParser aXml) throws XmlPullParserException, IOException {
		String _Result = "";
        int eventType = aXml.getEventType();
        while (!((eventType == XmlPullParser.END_TAG) && (aXml.getName().equals("websites")))) {
        	if ((eventType == XmlPullParser.START_TAG) && (aXml.getName().equals("url"))){
        		_Result = _Result + parseUrlTag(aXml);
            }
        	eventType = aXml.next();
        }		
        return _Result;
	}
	
	//CSS style for the html
    private String getStyle() {
        return String.format("<style type=\"text/css\">%s</style>", mStyle);
    }

    public void setStyle(final String style) {
        mStyle = style;
    }

    // now we' re able to set a custom icon, default is "no icon"
    private int getIcon() {
		return mIcon;
    }
    
    public void setCustomIcon(final int icon) {
		 mIcon = icon;
    }
       
    // and we can set a custom Title, default is "credits"
    private int getCustomTitle() {
		return mTitle ;
    }
    
    public void setCustomTitle(final int title) {
    	mTitle = title;
    }
    
	//Get the credits in html code, this will be shown in the dialog's webview
	private String getHTMLCredits(final int aResourceId, final Resources aResource) {
		String _Result = "<html><head>" + getStyle() + "</head><body>";
    	final XmlResourceParser _xml = aResource.getXml(aResourceId);
    	try
    	{
            int eventType = _xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
            	if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("section"))){
            		_Result = _Result + parseSectionTag(_xml);
      		
	            }
            	if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("copyright"))){
            		_Result = _Result + parseCopyrightTag(_xml);
      		
	            }
            	if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("websites"))){
            		_Result = _Result + parseWebsitesTag(_xml);
      		
	            }            	
            	eventType = _xml.next();
            }
    	} 
    	catch (final XmlPullParserException e)
    	{
    		Log.e(TAG, e.getMessage(), e);
    	}
    	catch (final IOException e)
    	{
    		Log.e(TAG, e.getMessage(), e);
    		
    	}        	
    	finally
    	{        	
    		_xml.close();
    	}		
		_Result = _Result + "</body></html>";
		return _Result;
	}
	
	//Call to show the credits dialog
    public void show() {
    	//Get resources
    	final String _PackageName = mContext.getPackageName();
    	Resources _Resource;
		try {
			_Resource = mContext.getPackageManager().getResourcesForApplication(_PackageName);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
			return;
		} 
		
//		removed this to let the developer even set a custom title 
//        //Get dialog title	        	
//   	int _resID = _Resource.getIdentifier(TITLE_CREDITS , "string", _PackageName);
//        final String _Title = _Resource.getString(_resID);
 
        //Get credits xml resource id
      	final int _resID = _Resource.getIdentifier(CREDITS_XML, "xml", _PackageName);
        //Create html credits
       	final String _HTML = getHTMLCredits(_resID, _Resource);
       	
        //Get button strings
        final String _Close =  _Resource.getString(R.string.credits_close);

        //Check for empty credits
        if (_HTML.equals("") == true)
        {
        	//Could not load credits, message user and exit
        	Toast.makeText(mContext, "Could not load credits", Toast.LENGTH_SHORT).show();
        	return;
        }
        //Create webview and load html
        final WebView _WebView = new WebView(mContext);
        _WebView.loadData(_HTML, "text/html", "utf-8");
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle(getCustomTitle())
                .setView(_WebView)
                .setIcon(getIcon())
                .setPositiveButton(_Close, new Dialog.OnClickListener() {
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

}
