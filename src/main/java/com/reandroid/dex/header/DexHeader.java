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
package com.reandroid.dex.header;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerItem;

import java.io.IOException;
import java.io.InputStream;

public class DexHeader extends FixedBlockContainer implements BlockLoad {

    public final Magic magic;
    public final Version version;
    public final Checksum checksum;
    public final Signature signature;

    public final IntegerItem fileSize;
    public final IntegerItem headerSize;
    public final Endian endian;
    public final IntegerItem map;

    public final OffsetAndCount strings;
    public final OffsetAndCount type;
    public final OffsetAndCount proto;
    public final OffsetAndCount field;
    public final OffsetAndCount method;
    public final OffsetAndCount class_def;
    public final OffsetAndCount data;

    public final ByteArray unknown;

    public DexHeader() {
        super(16);

        this.magic = new Magic();
        this.version = new Version();
        this.checksum = new Checksum();
        this.signature = new Signature();

        this.fileSize = new IntegerItem();
        this.headerSize = new IntegerItem();

        this.endian = new Endian();

        this.map = new IntegerItem();

        this.strings = new OffsetAndCount();
        this.type = new OffsetAndCount();
        this.proto = new OffsetAndCount();
        this.field = new OffsetAndCount();
        this.method = new OffsetAndCount();
        this.class_def = new OffsetAndCount();
        this.data = new OffsetAndCount();

        this.unknown = new ByteArray();

        addChild(0, magic);
        addChild(1, version);
        addChild(2, checksum);
        addChild(3, signature);
        addChild(4, fileSize);
        addChild(5, headerSize);
        addChild(6, endian);
        addChild(7, map);

        addChild(8, strings);
        addChild(9, type);
        addChild(10, proto);
        addChild(11, field);
        addChild(12, method);
        addChild(13, class_def);
        addChild(14, data);

        addChild(15, unknown);


        headerSize.setBlockLoad(this);


        this.version.putByteArray(0,
                new byte[]{(byte)'0', (byte)'3', (byte)'5', (byte)0x0});
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == headerSize){
            unknown.setSize(headerSize.get() - countBytes());
        }
    }

    @Override
    public String toString() {
        return "Header {" +
                "magic=" + magic +
                ", version=" + version +
                ", checksum=" + checksum +
                ", signature=" + signature +
                ", fileSize=" + fileSize +
                ", headerSize=" + headerSize +
                ", endian=" + endian +
                ", map=" + map +
                ", strings=" + strings +
                ", type=" + type +
                ", proto=" + proto +
                ", field=" + field +
                ", method=" + method +
                ", clazz=" + class_def +
                ", data=" + data +
                ", unknown=" + unknown +
                '}';
    }
    public static DexHeader readHeader(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[COMMON_HEADER_SIZE];
        int read = inputStream.read(bytes, 0, bytes.length);
        if(read < 0){
            throw new IOException("Finished reading");
        }
        if(read < bytes.length){
            throw new IOException("Few bytes to read header: " + read);
        }
        BlockReader reader = new BlockReader(bytes);
        DexHeader dexHeader = new DexHeader();
        //to protect from reading oversize headers
        dexHeader.headerSize.setBlockLoad(null);
        dexHeader.readBytes(reader);
        reader.close();
        return dexHeader;
    }

    private static final int COMMON_HEADER_SIZE = 112;
}
