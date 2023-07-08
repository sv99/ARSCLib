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
package com.reandroid.arsc.value;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.HexUtil;
import com.reandroid.json.JSONObject;

public class ResValueMap extends AttributeValue{

    public ResValueMap() {
        super(12, OFFSET_SIZE);
    }
    @Override
    public String decodeName(boolean includePrefix){
        int resourceId = getNameResourceID();
        if(!PackageBlock.isResourceId(resourceId)){
            AttributeType attributeType = getAttributeType();
            if(attributeType != null){
                return attributeType.getName();
            }
            return null;
        }
        ResourceEntry resourceEntry = resolve(resourceId);
        if(resourceEntry == null || !resourceEntry.isDeclared()){
            return ValueCoder.decodeUnknownResourceId(false, resourceId);
        }
        String name = resourceEntry.getName();
        if(includePrefix && resourceEntry.getPackageBlock() != getPackageBlock()){
            String prefix = resourceEntry.getPackageName();
            if(prefix != null){
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    @Override
    String decodeDataAsAttrFormats(){
        AttributeType attributeType = getAttributeType();
        if(attributeType != AttributeType.FORMATS){
            return null;
        }
        return AttributeDataFormat.toString(AttributeDataFormat.decodeValueTypes(getData()));
    }
    @Override
    public String decodePrefix(){
        ResourceEntry resourceEntry = resolveName();
        if(resourceEntry == null || getPackageBlock() == resourceEntry.getPackageBlock()){
            return null;
        }
        return resourceEntry.getPackageName();
    }
    public AttributeType getAttributeType(){
        return AttributeType.valueOf(getNameResourceID());
    }
    public void setAttributeType(AttributeType attributeType){
        setNameResourceID(attributeType.getId());
        if(attributeType == AttributeType.FORMATS && getValueType() == ValueType.NULL){
            setValueType(ValueType.DEC);
        }
    }
    public AttributeDataFormat[] getAttributeTypeFormats(){
        AttributeType attributeType = getAttributeType();
        if(attributeType != AttributeType.FORMATS){
            return null;
        }
        return AttributeDataFormat.decodeValueTypes(getData());
    }
    public void addAttributeTypeFormats(AttributeDataFormat... formats){
        if(formats == null){
            return;
        }
        int data = getData() | AttributeDataFormat.sum(formats);
        setData(data);
        if(getValueType() == ValueType.NULL){
            setValueType(ValueType.DEC);
        }
    }
    public void addAttributeTypeFormat(AttributeDataFormat format){
        if(format == null){
            return;
        }
        int data = getData() | format.getMask();
        setData(data);
    }
    public Entry getEntry(){
        return getParent(Entry.class);
    }
    @Override
    public PackageBlock getParentChunk(){
        Entry entry = getEntry();
        if(entry!=null){
            return entry.getPackageBlock();
        }
        return null;
    }

    public ResTableMapEntry getParentMapEntry(){
        return getParentInstance(ResTableMapEntry.class);
    }
    public Entry getParentEntry(){
        return getParentInstance(Entry.class);
    }

    public int getName(){
        return getInteger(getBytesInternal(), OFFSET_NAME);
    }
    public void setName(int name){
        putInteger(getBytesInternal(), OFFSET_NAME, name);
    }

    @Override
    public int getNameResourceID() {
        return getName();
    }
    @Override
    public void setNameResourceID(int resourceId){
        setName(resourceId);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        if(jsonObject==null){
            return null;
        }
        jsonObject.put(NAME_name, getName());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        setName(json.getInt(NAME_name));
    }

    public void setNameHigh(short val){
        int name = getName() & 0xffff;
        name = ((val & 0xffff) <<16 ) | name;
        setName(name);
    }
    public void setNameLow(short val){
        int name = getName() & 0xffff0000;
        name = (val & 0xffff) | name;
        setName(name);
    }
    public void setDataHigh(short val){
        int data = getData() & 0xffff;
        data = ((val & 0xffff) <<16 ) | data;
        setData(data);
    }
    public void setDataLow(short val){
        int data = getData() & 0xffff0000;
        data = (val & 0xffff) | data;
        setData(data);
    }
    @Override
    public void merge(ValueItem valueItem){
        if(valueItem==this || !(valueItem instanceof ResValueMap)){
            return;
        }
        ResValueMap resValueMap = (ResValueMap) valueItem;
        super.merge(resValueMap);
        setName(resValueMap.getName());
    }
    @Override
    public String toString(){
        String name = decodeName();
        String data = decodeValue();
        if(name != null && data != null){
            return name + "=\"" + data + "\"";
        }
        return "name=" + HexUtil.toHex8(getName())
                +", " + super.toString();
    }

    private static final int OFFSET_NAME = 0;
    private static final int OFFSET_SIZE = 4;

    public static final String NAME_name = "name";

}
