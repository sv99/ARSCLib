/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.xml;

import com.reandroid.utils.collection.IndexIterator;
import com.reandroid.utils.collection.SizedSupplier;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StyleElement extends XMLElement implements StyleNode{
    public StyleElement(String name){
        super(name);
    }
    public StyleElement(){
        this("");
    }
    public StyleElement getParentElement(){
        return (StyleElement) super.getParentElement();
    }
    void copyFrom(XMLElement xmlElement){
        setName(xmlElement.getName());
        Iterator<? extends XMLAttribute> attributes = xmlElement.getAttributes();
        while (attributes.hasNext()){
            addAttribute(new StyleAttribute(attributes.next()));
        }
        Iterator<XMLNode> iterator = xmlElement.iterator();
        while (iterator.hasNext()){
            XMLNode xmlNode = iterator.next();
            if(xmlNode instanceof XMLElement){
                StyleElement styleElement = newElement();
                add(styleElement);
                styleElement.copyFrom((XMLElement) xmlNode);
            }else if(xmlNode instanceof XMLText){
                XMLText xmlText = (XMLText)xmlNode;
                StyleText styleText = new StyleText(xmlText.getText());
                add(styleText);
            }
        }
    }
    @Override
    public StyleAttribute getAttributeAt(int i){
        return (StyleAttribute) super.getAttributeAt(i);
    }
    public void addAttribute(StyleAttribute attribute){
        super.addAttribute(attribute);
    }
    @Override
    public StyleAttribute getAttribute(String name){
        return (StyleAttribute) super.getAttribute(name);
    }
    public void addElement(StyleElement element){
        add(element);
    }
    @Override
    public Iterator<StyleElement> getElements(){
        return iterator(StyleElement.class);
    }
    @Override
    public Iterator<StyleAttribute> getAttributes() {
        return new IndexIterator<StyleAttribute>(new SizedSupplier<StyleAttribute>() {
            @Override
            public int size() {
                return getAttributeCount();
            }
            @Override
            public StyleAttribute get(int index) {
                return getAttributeAt(index);
            }
        });
    }

    public String getStyleableTag(){
        StringWriter writer = new StringWriter();
        writer.write(getName());
        try {
            appendAttributes(writer, false, false);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    public int getStart(){
        XMLNode parent = getParent();
        if(parent == null){
            return 0;
        }
        int result = 0;
        Iterator<XMLNode> itr = ((XMLNodeTree)parent).iterator();
        while (itr.hasNext()){
            XMLNode child = itr.next();
            if(child == this){
                break;
            }
            result += child.getLength();
        }
        return result;
    }
    public int getEnd(){
        return getEnd(getStart());
    }
    private int getEnd(int start){
        int result = start;
        Iterator<XMLNode> itr = iterator();
        while (itr.hasNext()){
            XMLNode child = itr.next();
            if(child == this){
                break;
            }
            result += child.getLength();
        }
        if(result >= start){
            result = result - 1;
        }
        return result;
    }
    @Override
    public int getLength(){
        int result = 0;
        Iterator<XMLNode> itr = iterator();
        while (itr.hasNext()){
            XMLNode child = itr.next();
            result += child.getLength();
        }
        return result;
    }
    void writeStyledText(Appendable appendable) throws IOException {
        Iterator<XMLNode> iterator = iterator();
        while (iterator.hasNext()){
            XMLNode xmlNode = iterator.next();
            if(xmlNode instanceof StyleText){
                StyleText styleText = (StyleText) xmlNode;
                styleText.writeStyledText(appendable);
            }else if(xmlNode instanceof StyleElement){
                StyleElement element = (StyleElement) xmlNode;
                element.writeStyledText(appendable);
            }
        }
    }
    @Override
    public void appendChar(char ch) {
        if(ch == 0){
            return;
        }
        XMLNode xmlNode = getLast();
        StyleText styleText;
        if(xmlNode instanceof StyleText){
            styleText = (StyleText) xmlNode;
        }else {
            styleText = new StyleText();
            add(styleText);
        }
        styleText.appendChar(ch);
    }
    @Override
    public StyleNode getParentStyle() {
        return (StyleNode) getParent();
    }
    @Override
    public void addStyleNode(StyleNode styleNode){
        add((XMLNode) styleNode);
    }
    @Override
    void startSerialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, getName());
        Iterator<StyleAttribute> itr = getAttributes();
        while (itr.hasNext()){
            itr.next().serialize(serializer);
        }
    }
    @Override
    void endSerialize(XmlSerializer serializer) throws IOException {
        serializer.endTag(null, getName());
    }
    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        if(event != XmlPullParser.START_TAG){
            throw new XmlPullParserException("Not START_TAG event");
        }
        setName(parser.getName());
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            addAttribute(new StyleAttribute(parser.getAttributeName(i),
                    parser.getAttributeValue(i)));
        }
        event = parser.next();
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT){
            if(event == XmlPullParser.START_TAG){
                StyleElement element = new StyleElement();
                addElement(element);
                element.parse(parser);
            }else if(XMLText.isTextEvent(event)){
                StyleText styleText = newText();
                add(styleText);
                styleText.parse(parser);
            }else {
                parser.next();
            }
            event = parser.getEventType();
        }
        if(parser.getEventType() == XmlPullParser.END_TAG){
            parser.next();
        }
    }

    @Override
    StyleElement newElement(){
        return new StyleElement();
    }
    @Override
    StyleText newText(){
        return new StyleText();
    }
    @Override
    XMLComment newComment(){
        return null;
    }
    @Override
    StyleAttribute newAttribute(){
        return new StyleAttribute();
    }

    @Override
    public String toString(){
        return "[" + getStart() + ", " + getEnd() + "] "
                + getStyleableTag();
    }
}
