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
package com.reandroid.common;

import java.io.*;

public class BytesOutputStream extends ByteArrayOutputStream {
    public BytesOutputStream(int initialCapacity){
        super(initialCapacity);
    }
    public BytesOutputStream(){
        this(32);
    }

    public int position() {
        return size();
    }
    public void write(InputStream inputStream) throws IOException{
        if(inputStream instanceof BytesInputStream){
            write((BytesInputStream) inputStream);
            return;
        }
        int bufferStep = 500;
        int maxBuffer = 4096 * 20;
        int length;
        byte[] buffer = new byte[2048];
        while ((length = inputStream.read(buffer, 0, buffer.length)) >= 0){
            write(buffer, 0, length);
            if(buffer.length < maxBuffer){
                buffer = new byte[buffer.length + bufferStep];
            }
        }
        inputStream.close();
    }
    public void write(BytesInputStream bis) throws IOException {
        byte[] bytes = bis.toByteArray();
        write(bytes, 0, bytes.length);
    }
    @Override
    public String toString(){
        return "pos = " + size();
    }
}
