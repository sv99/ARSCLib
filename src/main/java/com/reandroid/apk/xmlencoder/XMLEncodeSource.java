package com.reandroid.apk.xmlencoder;

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.CrcOutputStream;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.source.XMLParserSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class XMLEncodeSource extends ByteInputSource {
    private final PackageBlock packageBlock;
    private final XMLParserSource parserSource;
    private APKLogger mLogger;
    private byte[] array;

    public XMLEncodeSource(PackageBlock packageBlock, XMLParserSource parserSource) {
        super(DISPOSED, parserSource.getPath());
        this.packageBlock = packageBlock;
        this.parserSource = parserSource;
    }
    @Override
    public long getLength() throws IOException {
        return getArray().length;
    }
    @Override
    public long getCrc() throws IOException{
        CRC32 crc32 = new CRC32();
        byte[] bytes = getArray();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        byte[] bytes = getArray();
        if(bytes == DISPOSED){
            throw new IOException("Disposed source: " + getAlias());
        }
        outputStream.write(bytes, 0, bytes.length);
        return bytes.length;
    }
    @Override
    public byte[] getBytes() {
        try {
            return getArray();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    @Override
    public void disposeInputSource(){
        array = DISPOSED;
    }
    private byte[] getArray() throws IOException{
        if(array != null){
            return array;
        }
        try {
            array = encode().getBytes();
        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
        return array;
    }
    private ResXmlDocument encode() throws XmlPullParserException, IOException {
        XMLParserSource parserSource = this.parserSource;
        logVerbose("Encoding: " + parserSource.getPath());
        XmlPullParser parser = parserSource.getParser();
        ResXmlDocument resXmlDocument = new ResXmlDocument();
        resXmlDocument.setPackageBlock(this.packageBlock);
        resXmlDocument.parse(parser);
        IOUtil.close(parser);
        return resXmlDocument;
    }
    public void setApkLogger(APKLogger logger){
        this.mLogger = logger;
    }
    private void logVerbose(String msg){
        APKLogger logger = this.mLogger;
        if(logger != null){
            logger.logVerbose(msg);
        }
    }
    private static final byte[] DISPOSED = new byte[0];
}
